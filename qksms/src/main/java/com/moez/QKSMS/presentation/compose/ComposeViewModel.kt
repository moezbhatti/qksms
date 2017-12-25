package com.moez.QKSMS.presentation.compose

import android.content.Context
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.ClipboardUtils
import com.moez.QKSMS.common.util.extensions.asObservable
import com.moez.QKSMS.common.util.extensions.makeToast
import com.moez.QKSMS.common.util.filter.ContactFilter
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.ContactRepository
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.*
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.common.base.QkViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ComposeViewModel(threadId: Long, body: String)
    : QkViewModel<ComposeView, ComposeState>(ComposeState(editingMode = threadId == 0L, draft = body)) {

    @Inject lateinit var context: Context
    @Inject lateinit var contactFilter: ContactFilter
    @Inject lateinit var contactsRepo: ContactRepository
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var syncContacts: ContactSync
    @Inject lateinit var markArchived: MarkArchived
    @Inject lateinit var markUnarchived: MarkUnarchived
    @Inject lateinit var deleteConversation: DeleteConversation
    @Inject lateinit var sendMessage: SendMessage
    @Inject lateinit var markRead: MarkRead
    @Inject lateinit var deleteMessage: DeleteMessage

    private val contacts: List<Contact> by lazy { contactsRepo.getContacts() }
    private val contactsReducer: Subject<(List<Contact>) -> List<Contact>> = PublishSubject.create()
    private val selectedContacts: Observable<List<Contact>>
    private val conversation: Observable<Conversation>

    init {
        appComponent.inject(this)

        selectedContacts = contactsReducer
                .scan(listOf<Contact>(), { previousState, reducer -> reducer(previousState) })
                .doOnNext { contacts -> newState { it.copy(selectedContacts = contacts) } }

        val initialConversation: Observable<Conversation> = when (threadId) {
            0L -> Observable.empty()
            else -> messageRepo.getConversationAsync(threadId).asObservable()
        }

        // Map the selected contacts to a conversation so that we can display the message history
        val selectedConversation = selectedContacts
                .map { contacts -> contacts.map { it.numbers.firstOrNull()?.address ?: "" } }
                .flatMapMaybe { addresses -> messageRepo.getOrCreateConversation(addresses) }

        // Merges two potential conversation sources (threadId from constructor and contact selection) into a single
        // stream of conversations. If the conversation was deleted, notify the activity to shut down
        conversation = initialConversation
                .filter { conversation -> conversation.isLoaded }
                .doOnNext { conversation ->
                    if (!conversation.isValid) {
                        newState { it.copy(hasError = true) }
                    }
                }
                .mergeWith(selectedConversation)
                .filter { conversation -> conversation.isValid }
                .filter { conversation -> conversation.id != 0L }
                .distinctUntilChanged()
                .doOnNext { conversation ->
                    newState { it.copy(title = conversation.getTitle(), archived = conversation.archived) }
                }

        disposables += sendMessage
        disposables += markRead
        disposables += markArchived
        disposables += markUnarchived
        disposables += deleteConversation
        disposables += conversation.subscribe()

        // When the conversation changes, update the messages for the adapter
        // When the message list changes, make sure to mark them read
        disposables += conversation
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .map { conversation -> messageRepo.getMessages(conversation.id) }
                .doOnNext { messages -> newState { it.copy(messages = messages) } }
                .flatMap { messages -> messages.asObservable() }
                .withLatestFrom(conversation, { messages, conversation ->
                    markRead.execute(conversation.id)
                    messages
                })
                .subscribe()

        if (threadId == 0L) {
            syncContacts.execute(Unit)
        }
    }

    override fun bindView(view: ComposeView) {
        super.bindView(view)

        // Set the contact suggestions list to visible at all times when in editing mode and there are no contacts
        // selected yet, and also visible while in editing mode and there is text entered in the query field
        intents += Observables
                .combineLatest(view.queryChangedIntent, selectedContacts, { query, selectedContacts ->
                    selectedContacts.isEmpty() || query.isNotEmpty()
                })
                .skipUntil(state.filter { state -> state.editingMode == true })
                .takeUntil(state.filter { state -> state.editingMode == false })
                .distinctUntilChanged()
                .subscribe { contactsVisible -> newState { it.copy(contactsVisible = contactsVisible && it.editingMode) } }

        // Update the list of contact suggestions based on the query input, while also filtering out any contacts
        // that have already been selected
        intents += Observables
                .combineLatest(view.queryChangedIntent, selectedContacts, { query, selectedContacts ->
                    contacts
                            .filterNot { contact -> selectedContacts.contains(contact) }
                            .filter { contact -> contactFilter.filter(contact, query) }
                })
                .skipUntil(state.filter { state -> state.editingMode == true })
                .takeUntil(state.filter { state -> state.editingMode == false })
                .subscribeOn(Schedulers.computation())
                .subscribe { contacts -> newState { it.copy(contacts = contacts) } }

        // Update the list of selected contacts when a new contact is selected or an existing one is deselected
        intents += Observable.merge(
                view.chipDeletedIntent.doOnNext { contact ->
                    contactsReducer.onNext { contacts -> contacts.filterNot { it == contact } }
                },
                view.chipSelectedIntent.doOnNext { contact ->
                    contactsReducer.onNext { contacts -> contacts.toMutableList().apply { add(contact) } }
                })
                .skipUntil(state.filter { state -> state.editingMode == true })
                .takeUntil(state.filter { state -> state.editingMode == false })
                .subscribe()

        // When the menu is loaded, trigger a new state so that the menu options can be rendered correctly
        intents += view.menuReadyIntent.subscribe {
            newState { it.copy() }
        }

        // Open the phone dialer if the call button is clicked
        intents += view.callIntent
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .map { conversation -> conversation.recipients.first() }
                .map { recipient -> recipient.address }
                .subscribe { address -> navigator.makePhoneCall(address) }

        // Toggle the archived state of the conversation
        intents += view.archiveIntent
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .subscribe { conversation ->
                    when (conversation.archived) {
                        true -> markUnarchived.execute(conversation.id, { context.makeToast(R.string.toast_unarchived) })
                        false -> markArchived.execute(conversation.id, { context.makeToast(R.string.toast_archived) })
                    }
                }

        // Delete the conversation
        intents += view.deleteIntent
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .subscribe { conversation -> deleteConversation.execute(conversation.id) }

        // Add an attachment
        intents += view.attachIntent
                .subscribe { context.makeToast(R.string.compose_attach_unsupported) }

        // Enable the send button when there is text input into the new message body, disable otherwise
        intents += view.textChangedIntent.subscribe { text ->
            newState { it.copy(draft = text.toString(), canSend = text.isNotEmpty()) }
        }

        // Send a message when the send button is clicked, and disable editing mode if it's enabled
        intents += view.sendIntent
                .withLatestFrom(view.textChangedIntent, { _, body -> body })
                .map { body -> body.toString() }
                .withLatestFrom(conversation, { body, conversation ->
                    val threadId = conversation.id
                    val address = conversation.recipients.first()?.address.orEmpty()
                    sendMessage.execute(SendMessage.Params(threadId, address, body))
                    newState { it.copy(editingMode = false, draft = "", canSend = false) }
                })
                .subscribe()

        // Copy the body of the selected message into the clipboard
        intents += view.copyTextIntent.subscribe { message ->
            ClipboardUtils.copy(context, message.body)
            context.makeToast(R.string.toast_copied)
        }

        // Forward the message
        intents += view.forwardMessageIntent.subscribe { message ->
            navigator.showCompose(message.body)
        }

        // Delete the selected message
        intents += view.deleteMessageIntent.subscribe { message ->
            deleteMessage.execute(message.id)
        }
    }

}