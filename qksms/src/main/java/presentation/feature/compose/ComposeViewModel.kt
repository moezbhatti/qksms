/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package presentation.feature.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.ClipboardUtils
import common.util.extensions.asObservable
import common.util.extensions.makeToast
import common.util.filter.ContactFilter
import data.model.Contact
import data.model.Conversation
import data.model.Message
import data.repository.ContactRepository
import data.repository.MessageRepository
import interactor.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import presentation.common.Navigator
import presentation.common.base.QkViewModel
import java.net.URLDecoder
import javax.inject.Inject

class ComposeViewModel(intent: Intent) : QkViewModel<ComposeView, ComposeState>(ComposeState()) {

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

    private var draft: String = ""
    private val attachments: Subject<List<Uri>> = BehaviorSubject.createDefault(ArrayList())
    private val contacts: Observable<List<Contact>> by lazy { contactsRepo.getUnmanagedContacts().toObservable() }
    private val contactsReducer: Subject<(List<Contact>) -> List<Contact>> = PublishSubject.create()
    private val selectedContacts: Observable<List<Contact>>
    private val conversation: Observable<Conversation>
    private val messages: Observable<List<Message>>

    init {
        appComponent.inject(this)

        draft = intent.extras?.getString(Intent.EXTRA_TEXT) ?: ""
        intent.clipData
        val threadId = intent.extras?.getLong("threadId") ?: 0L
        var address = ""

        intent.data?.let {
            val data = it.toString()
            address = when {
                it.scheme.startsWith("smsto") -> data.replace("smsto:", "")
                it.scheme.startsWith("mmsto") -> data.replace("mmsto:", "")
                it.scheme.startsWith("sms") -> data.replace("sms:", "")
                it.scheme.startsWith("mms") -> data.replace("mms:", "")
                else -> ""
            }

            // The dialer app on Oreo sends a URL encoded string, make sure to decode it
            if (address.contains('%')) address = URLDecoder.decode(address, "UTF-8")
        }

        val initialConversation: Observable<Conversation> = when {
            threadId != 0L -> {
                newState { ComposeState(editingMode = false) }
                messageRepo.getConversationAsync(threadId).asObservable()
            }

            address.isNotBlank() -> {
                newState { ComposeState(editingMode = false) }
                messageRepo.getOrCreateConversation(address).toObservable()
            }

            else -> {
                newState { ComposeState(editingMode = true) }
                Observable.empty()
            }
        }

        selectedContacts = contactsReducer
                .scan(listOf<Contact>(), { previousState, reducer -> reducer(previousState) })
                .doOnNext { contacts -> newState { it.copy(selectedContacts = contacts) } }

        // Map the selected contacts to a conversation so that we can display the message history
        val selectedConversation = selectedContacts
                .skipUntil(state.filter { state -> state.editingMode })
                .takeUntil(state.filter { state -> !state.editingMode })
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

        // When the conversation changes, update the messages for the adapter
        messages = conversation
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .map { conversation -> messageRepo.getMessages(conversation.id) }
                .doOnNext { messages -> newState { it.copy(messages = messages) } }
                .switchMap { messages -> messages.asObservable() }

        disposables += sendMessage
        disposables += markRead
        disposables += markArchived
        disposables += markUnarchived
        disposables += deleteConversation
        disposables += conversation.subscribe()
        disposables += messages.subscribe()
        disposables += attachments.subscribe { attachments -> newState { it.copy(attachments = attachments) } }

        if (threadId == 0L) {
            syncContacts.execute(Unit)
        }
    }

    override fun bindView(view: ComposeView) {
        super.bindView(view)

        if (draft.isNotEmpty()) {
            view.setDraft(draft)
            draft = ""
        }

        // Set the contact suggestions list to visible at all times when in editing mode and there are no contacts
        // selected yet, and also visible while in editing mode and there is text entered in the query field
        Observables
                .combineLatest(view.queryChangedIntent, selectedContacts, { query, selectedContacts ->
                    selectedContacts.isEmpty() || query.isNotEmpty()
                })
                .skipUntil(state.filter { state -> state.editingMode })
                .takeUntil(state.filter { state -> !state.editingMode })
                .distinctUntilChanged()
                .autoDisposable(view.scope())
                .subscribe { contactsVisible -> newState { it.copy(contactsVisible = contactsVisible && it.editingMode) } }

        // Update the list of contact suggestions based on the query input, while also filtering out any contacts
        // that have already been selected
        Observables
                .combineLatest(view.queryChangedIntent, contacts, selectedContacts, { query, contacts, selectedContacts ->
                    contacts
                            .filterNot { contact -> selectedContacts.contains(contact) }
                            .filter { contact -> contactFilter.filter(contact, query) }
                })
                .skipUntil(state.filter { state -> state.editingMode })
                .takeUntil(state.filter { state -> !state.editingMode })
                .subscribeOn(Schedulers.computation())
                .autoDisposable(view.scope())
                .subscribe { contacts -> newState { it.copy(contacts = contacts) } }

        // Update the list of selected contacts when a new contact is selected or an existing one is deselected
        Observable.merge(
                view.chipDeletedIntent.doOnNext { contact ->
                    contactsReducer.onNext { contacts -> contacts.filterNot { it == contact } }
                },
                view.chipSelectedIntent.doOnNext { contact ->
                    contactsReducer.onNext { contacts -> contacts.toMutableList().apply { add(contact) } }
                })
                .skipUntil(state.filter { state -> state.editingMode })
                .takeUntil(state.filter { state -> !state.editingMode })
                .autoDisposable(view.scope())
                .subscribe()

        // When the menu is loaded, trigger a new state so that the menu options can be rendered correctly
        view.menuReadyIntent
                .autoDisposable(view.scope())
                .subscribe { newState { it.copy() } }

        // Open the phone dialer if the call button is clicked
        view.callIntent
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .map { conversation -> conversation.recipients.first() }
                .map { recipient -> recipient.address }
                .autoDisposable(view.scope())
                .subscribe { address -> navigator.makePhoneCall(address) }

        // Toggle the archived state of the conversation
        view.archiveIntent
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .autoDisposable(view.scope())
                .subscribe { conversation ->
                    when (conversation.archived) {
                        true -> markUnarchived.execute(conversation.id, { context.makeToast(R.string.toast_unarchived) })
                        false -> markArchived.execute(conversation.id, { context.makeToast(R.string.toast_archived) })
                    }
                }

        // Delete the conversation
        view.deleteIntent
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .autoDisposable(view.scope())
                .subscribe { conversation -> deleteConversation.execute(conversation.id) }

        // Mark the conversation read, if in foreground
        Observables.combineLatest(messages, view.activityVisibleIntent, { _, b -> b })
                .withLatestFrom(conversation, { visible, conversation ->
                    if (visible) markRead.execute(conversation.id)
                })
                .autoDisposable(view.scope())
                .subscribe()

        // Attach a photo
        view.attachIntent
                .flatMap { RxImagePicker.with(context).requestImage(Sources.GALLERY) }
                .withLatestFrom(attachments, { attachment, attachments -> attachments + attachment })
                .autoDisposable(view.scope())
                .subscribe { attachments.onNext(it) }

        // Detach a photo
        view.attachmentDeletedIntent
                .withLatestFrom(attachments, { bitmap, attachments -> attachments.filter { it !== bitmap } })
                .autoDisposable(view.scope())
                .subscribe { attachments.onNext(it) }

        // Enable the send button when there is text input into the new message body or there's
        // an attachment, disable otherwise
        Observables
                .combineLatest(view.textChangedIntent, attachments, { text, attachments ->
                    text.isNotBlank() || attachments.isNotEmpty()
                })
                .autoDisposable(view.scope())
                .subscribe { canSend -> newState { it.copy(canSend = canSend) } }

        // Send a message when the send button is clicked, and disable editing mode if it's enabled
        view.sendIntent
                .withLatestFrom(view.textChangedIntent, { _, body -> body })
                .map { body -> body.toString() }
                .withLatestFrom(attachments, conversation, { body, attachments, conversation ->
                    val threadId = conversation.id
                    val addresses = conversation.recipients.map { it.address }
                    sendMessage.execute(SendMessage.Params(threadId, addresses, body, attachments))
                    view.setDraft("")
                    newState { it.copy(attachments = ArrayList()) }
                })
                .withLatestFrom(state, { _, state ->
                    if (state.editingMode) {
                        newState { it.copy(editingMode = false) }
                    }
                })
                .autoDisposable(view.scope())
                .subscribe()

        // Copy the body of the selected message into the clipboard
        view.copyTextIntent
                .autoDisposable(view.scope())
                .subscribe { message ->
                    ClipboardUtils.copy(context, message.body)
                    context.makeToast(R.string.toast_copied)
                }

        // Forward the message
        view.forwardMessageIntent
                .autoDisposable(view.scope())
                .subscribe { message -> navigator.showCompose(message.body) }

        // Delete the selected message
        view.deleteMessageIntent
                .autoDisposable(view.scope())
                .subscribe { message -> deleteMessage.execute(message.id) }
    }

}