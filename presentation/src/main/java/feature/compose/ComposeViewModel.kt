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
import android.telephony.PhoneNumberUtils
import android.telephony.SmsMessage
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import injection.appComponent
import common.util.ClipboardUtils
import common.util.extensions.makeToast
import common.util.filter.ContactFilter
import interactor.ContactSync
import interactor.DeleteMessage
import interactor.MarkRead
import interactor.RetrySending
import interactor.SendMessage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmList
import model.Contact
import model.Conversation
import model.Message
import model.PhoneNumber
import presentation.common.Navigator
import presentation.common.base.QkViewModel
import repository.ContactRepository
import repository.MessageRepository
import util.extensions.asObservable
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ComposeViewModel(intent: Intent) : QkViewModel<ComposeView, ComposeState>(ComposeState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var contactFilter: ContactFilter
    @Inject lateinit var contactsRepo: ContactRepository
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var syncContacts: ContactSync
    @Inject lateinit var sendMessage: SendMessage
    @Inject lateinit var retrySending: RetrySending
    @Inject lateinit var markRead: MarkRead
    @Inject lateinit var deleteMessage: DeleteMessage

    private var sharedText: String = ""
    private val attachments: Subject<List<Uri>> = BehaviorSubject.createDefault(ArrayList())
    private val contacts: Observable<List<Contact>> by lazy { contactsRepo.getUnmanagedContacts().toObservable() }
    private val contactsReducer: Subject<(List<Contact>) -> List<Contact>> = PublishSubject.create()
    private val selectedContacts: Observable<List<Contact>>
    private val conversation: Observable<Conversation>
    private val messages: Observable<List<Message>>

    init {
        appComponent.inject(this)

        sharedText = intent.extras?.getString(Intent.EXTRA_TEXT) ?: ""

        // If there are any image attachments, we'll set those as the initial attachments for the
        // conversation
        val sharedImages = mutableListOf<Uri>()
        intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.run { sharedImages += this }
        intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.run { sharedImages += this }
        attachments.onNext(sharedImages)

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
                newState { it.copy(selectedConversation = threadId, editingMode = false) }
                messageRepo.getConversationAsync(threadId).asObservable()
            }

            address.isNotBlank() -> {
                newState { it.copy(editingMode = false) }
                messageRepo.getOrCreateConversation(address).toObservable()
            }

            else -> {
                newState { it.copy(editingMode = true) }
                Observable.empty()
            }
        }

        selectedContacts = contactsReducer
                .scan(listOf<Contact>(), { previousState, reducer -> reducer(previousState) })
                .doOnNext { contacts -> newState { it.copy(selectedContacts = contacts) } }

        // Merges two potential conversation sources (threadId from constructor and contact selection) into a single
        // stream of conversations. If the conversation was deleted, notify the activity to shut down
        conversation = selectedContacts
                .skipUntil(state.filter { state -> state.editingMode })
                .takeUntil(state.filter { state -> !state.editingMode })
                .map { contacts -> contacts.map { it.numbers.firstOrNull()?.address ?: "" } }
                .flatMapMaybe { addresses -> messageRepo.getOrCreateConversation(addresses) }
                .mergeWith(initialConversation)
                .filter { conversation -> conversation.isLoaded }
                .doOnNext { conversation ->
                    if (!conversation.isValid) {
                        newState { it.copy(hasError = true) }
                    }
                }
                .filter { conversation -> conversation.isValid }
                .filter { conversation -> conversation.id != 0L }
                .distinctUntilChanged()

        // When the conversation changes, update the threadId and the messages for the adapter
        messages = conversation
                .distinctUntilChanged { conversation -> conversation.id }
                .observeOn(AndroidSchedulers.mainThread())
                .map { conversation ->
                    val messages = messageRepo.getMessages(conversation.id)
                    newState { it.copy(selectedConversation = conversation.id, messages = Pair(conversation, messages)) }
                    messages
                }
                .switchMap { messages -> messages.asObservable() }

        disposables += sendMessage
        disposables += markRead
        disposables += conversation.subscribe()
        disposables += messages.subscribe()

        disposables += conversation
                .distinctUntilChanged { conversation -> conversation.getTitle() }
                .subscribe { conversation ->
                    newState { it.copy(title = conversation.getTitle()) }
                }

        disposables += conversation
                .distinctUntilChanged { conversation -> conversation.archived }
                .subscribe { conversation ->
                    newState { it.copy(archived = conversation.archived) }
                }

        disposables += attachments
                .subscribe { attachments ->
                    newState { it.copy(attachments = attachments) }
                }

        if (threadId == 0L) {
            syncContacts.execute(Unit)
        }
    }

    override fun bindView(view: ComposeView) {
        super.bindView(view)

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
                    var filteredContacts = contacts
                            .filterNot { contact -> selectedContacts.contains(contact) }
                            .filter { contact -> contactFilter.filter(contact, query) }

                    if (PhoneNumberUtils.isWellFormedSmsAddress(query.toString())) {
                        filteredContacts = listOf(Contact(numbers = RealmList(PhoneNumber(address = query.toString())))) + filteredContacts
                    }

                    filteredContacts
                })
                .skipUntil(state.filter { state -> state.editingMode })
                .takeUntil(state.filter { state -> !state.editingMode })
                .subscribeOn(Schedulers.computation())
                .autoDisposable(view.scope())
                .subscribe { contacts -> newState { it.copy(contacts = contacts) } }

        // Backspaces should delete the most recent contact if there's no text input
        // Close the activity if user presses back
        view.queryKeyEventIntent
                .filter { event -> event.action == KeyEvent.ACTION_DOWN }
                .withLatestFrom(selectedContacts, view.queryChangedIntent, { event, contacts, query ->
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_DEL -> {
                            if (contacts.isNotEmpty() && query.isEmpty()) {
                                contactsReducer.onNext { it.dropLast(1) }
                            }
                        }

                        KeyEvent.KEYCODE_BACK -> {
                            newState { it.copy(hasError = true) }
                        }
                    }

                })
                .autoDisposable(view.scope())
                .subscribe()

        // Enter the first contact suggestion if the enter button is pressed
        view.queryEditorActionIntent
                .filter { actionId -> actionId == EditorInfo.IME_ACTION_DONE }
                .withLatestFrom(state, { _, state -> state })
                .autoDisposable(view.scope())
                .subscribe { state ->
                    state.contacts.firstOrNull()?.let { contact ->
                        contactsReducer.onNext { contacts -> contacts + contact }
                    }
                }

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

        // Open the conversation settings if info button is clicked
        view.infoIntent
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .autoDisposable(view.scope())
                .subscribe { conversation -> navigator.showConversationInfo(conversation.id) }

        view.messageClickIntent
                .filter { message -> message.isFailedMessage() }
                .doOnNext { message -> retrySending.execute(message) }
                .autoDisposable(view.scope())
                .subscribe()

        // Mark the conversation read, if in foreground
        Observables.combineLatest(messages, view.activityVisibleIntent, { _, b -> b })
                .withLatestFrom(conversation, { visible, conversation ->
                    if (visible && conversation.isValid) markRead.execute(conversation.id)
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

        conversation
                .map { conversation -> conversation.draft }
                .distinctUntilChanged()
                .autoDisposable(view.scope())
                .subscribe { draft ->

                    // If text was shared into the conversation, it should take priority over the
                    // existing draft
                    //
                    // TODO: Show dialog warning user about overwriting draft
                    if (sharedText.isNotBlank()) {
                        view.setDraft(sharedText)
                    } else {
                        view.setDraft(draft)
                    }
                }

        // Enable the send button when there is text input into the new message body or there's
        // an attachment, disable otherwise
        Observables
                .combineLatest(view.textChangedIntent, attachments, { text, attachments ->
                    text.isNotBlank() || attachments.isNotEmpty()
                })
                .autoDisposable(view.scope())
                .subscribe { canSend -> newState { it.copy(canSend = canSend) } }

        // Show the remaining character counter when necessary
        view.textChangedIntent
                .observeOn(Schedulers.computation())
                .map { draft -> SmsMessage.calculateLength(draft, false) }
                .map { array ->
                    val messages = array[0]
                    val remaining = array[2]

                    when {
                        messages <= 1 && remaining > 10 -> ""
                        messages <= 1 && remaining <= 10 -> "$remaining"
                        else -> "$remaining / $messages"
                    }
                }
                .distinctUntilChanged()
                .autoDisposable(view.scope())
                .subscribe { remaining -> newState { it.copy(remaining = remaining) } }

        // Update the draft whenever the text is changed
        view.textChangedIntent
                .debounce(100, TimeUnit.MILLISECONDS)
                .map { draft -> draft.toString() }
                .observeOn(Schedulers.io())
                .withLatestFrom(conversation.map { it.id }, { draft, threadId ->
                    messageRepo.saveDraft(threadId, draft)
                })
                .autoDisposable(view.scope())
                .subscribe()

        // Send a message when the send button is clicked, and disable editing mode if it's enabled
        view.sendIntent
                .withLatestFrom(view.textChangedIntent, { _, body -> body })
                .map { body -> body.toString() }
                .withLatestFrom(attachments, conversation, { body, attachments, conversation ->
                    val threadId = conversation.id
                    val addresses = conversation.recipients.map { it.address }
                    sendMessage.execute(SendMessage.Params(threadId, addresses, body, attachments))
                    view.setDraft("")
                    this.attachments.onNext(ArrayList())
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