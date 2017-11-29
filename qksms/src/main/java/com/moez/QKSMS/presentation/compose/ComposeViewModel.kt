package com.moez.QKSMS.presentation.compose

import android.content.Context
import android.telephony.PhoneNumberUtils
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.ClipboardUtils
import com.moez.QKSMS.common.util.extensions.asObservable
import com.moez.QKSMS.common.util.extensions.makeToast
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.ContactRepository
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.DeleteMessage
import com.moez.QKSMS.domain.interactor.MarkRead
import com.moez.QKSMS.domain.interactor.SendMessage
import com.moez.QKSMS.presentation.base.QkViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ComposeViewModel(threadId: Long) : QkViewModel<ComposeView, ComposeState>(ComposeState(editingMode = threadId == 0L)) {

    @Inject lateinit var context: Context
    @Inject lateinit var contactsRepo: ContactRepository
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var sendMessage: SendMessage
    @Inject lateinit var markRead: MarkRead
    @Inject lateinit var deleteMessage: DeleteMessage

    private val contactsReducer: Subject<(List<Contact>) -> List<Contact>> = PublishSubject.create()

    private val contacts: List<Contact>

    private val conversation: Observable<Conversation>

    init {
        AppComponentManager.appComponent.inject(this)

        // Merges two potential conversation sources (threadId from constructor and contact selection) into a single
        // stream of conversations
        val selectedConversation = contactsReducer
                .scan(listOf<Contact>(), { previousState, reducer -> reducer(previousState) })
                .doOnNext { contacts -> newState { it.copy(selectedContacts = contacts) } }
                .map { contacts -> contacts.map { it.address } }
                .flatMapMaybe { addresses -> messageRepo.getOrCreateConversation(addresses) }

        conversation = messageRepo.getConversationAsync(threadId)
                .asObservable<Conversation>()
                .filter { conversation -> conversation.isLoaded }
                .filter { conversation -> conversation.isValid }
                .mergeWith(selectedConversation)
                .distinctUntilChanged { conversation -> conversation.id }
                .doOnNext { conversation -> newState { it.copy(title = conversation.getTitle()) } }

        disposables += sendMessage
        disposables += markRead
        disposables += conversation.subscribe()

        // When the conversation changes, update the messages for the adapter
        // When the message list changes, make sure to mark them read
        disposables += conversation
                .map { conversation -> messageRepo.getMessages(conversation.id) }
                .doOnNext { messages -> newState { it.copy(messages = messages) } }
                .flatMap { messages -> messages.asObservable() }
                .withLatestFrom(conversation, { messages, conversation ->
                    markRead.execute(conversation.id)
                    messages
                })
                .subscribe()

        contacts = contactsRepo.getContacts()
    }

    override fun bindView(view: ComposeView) {
        super.bindView(view)

        intents += view.queryChangedIntent
                .toFlowable(BackpressureStrategy.LATEST)
                .map { query -> query.toString() }
                .map { query ->
                    contacts.filter { contact ->
                        contact.name.contains(query, true) || PhoneNumberUtils.compare(contact.address, query)
                    }
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contacts -> newState { it.copy(contacts = contacts) } }

        intents += view.chipDeletedIntent.subscribe { contact ->
            contactsReducer.onNext { contacts -> contacts.filterNot { it == contact } }
        }

        intents += view.chipSelectedIntent.subscribe { contact ->
            contactsReducer.onNext { contacts -> contacts.toMutableList().apply { add(contact) } }
        }

        intents += view.textChangedIntent.subscribe { text ->
            newState { it.copy(draft = text.toString(), canSend = text.isNotEmpty()) }
        }

        intents += view.sendIntent
                .withLatestFrom(view.textChangedIntent, { _, body -> body })
                .map { body -> body.toString() }
                .withLatestFrom(conversation, { body, conversation ->
                    val threadId = conversation.id
                    val address = conversation.contacts.first()?.address.orEmpty()
                    sendMessage.execute(SendMessage.Params(threadId, address, body))
                    newState { it.copy(draft = "", canSend = false) }
                })
                .subscribe()

        intents += view.copyTextIntent.subscribe { message ->
            ClipboardUtils.copy(context, message.body)
            context.makeToast(R.string.toast_copied)
        }

        intents += view.deleteMessageIntent.subscribe { message ->
            deleteMessage.execute(message.id)
        }
    }

}