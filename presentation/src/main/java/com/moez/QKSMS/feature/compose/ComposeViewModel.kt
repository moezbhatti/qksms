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
package com.moez.QKSMS.feature.compose

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.SmsMessage
import android.view.inputmethod.EditorInfo
import androidx.core.widget.toast
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkViewModel
import com.moez.QKSMS.common.util.BillingManager
import com.moez.QKSMS.common.util.ClipboardUtils
import com.moez.QKSMS.common.util.MessageDetailsFormatter
import com.moez.QKSMS.common.util.extensions.makeToast
import com.moez.QKSMS.compat.SubscriptionManagerCompat
import com.moez.QKSMS.compat.TelephonyCompat
import com.moez.QKSMS.extensions.asObservable
import com.moez.QKSMS.extensions.isImage
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.extensions.removeAccents
import com.moez.QKSMS.filter.ContactFilter
import com.moez.QKSMS.interactor.AddScheduledMessage
import com.moez.QKSMS.interactor.CancelDelayedMessage
import com.moez.QKSMS.interactor.ContactSync
import com.moez.QKSMS.interactor.DeleteMessages
import com.moez.QKSMS.interactor.MarkRead
import com.moez.QKSMS.interactor.RetrySending
import com.moez.QKSMS.interactor.SendMessage
import com.moez.QKSMS.manager.ActiveConversationManager
import com.moez.QKSMS.manager.PermissionManager
import com.moez.QKSMS.model.Attachment
import com.moez.QKSMS.model.Contact
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.PhoneNumber
import com.moez.QKSMS.repository.ContactRepository
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.MessageRepository
import com.moez.QKSMS.util.ActiveSubscriptionObservable
import com.moez.QKSMS.util.Preferences
import com.moez.QKSMS.util.tryOrNull
import com.uber.autodispose.kotlin.autoDisposable
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
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class ComposeViewModel @Inject constructor(
        @Named("query") private val query: String,
        @Named("threadId") private val threadId: Long,
        @Named("address") private val address: String,
        @Named("text") private val sharedText: String,
        @Named("attachments") private val sharedAttachments: List<Attachment>,
        private val context: Context,
        private val activeConversationManager: ActiveConversationManager,
        private val addScheduledMessage: AddScheduledMessage,
        private val billingManager: BillingManager,
        private val cancelMessage: CancelDelayedMessage,
        private val contactFilter: ContactFilter,
        private val contactsRepo: ContactRepository,
        private val conversationRepo: ConversationRepository,
        private val deleteMessages: DeleteMessages,
        private val markRead: MarkRead,
        private val messageDetailsFormatter: MessageDetailsFormatter,
        private val messageRepo: MessageRepository,
        private val navigator: Navigator,
        private val permissionManager: PermissionManager,
        private val prefs: Preferences,
        private val retrySending: RetrySending,
        private val sendMessage: SendMessage,
        private val subscriptionManager: SubscriptionManagerCompat,
        private val syncContacts: ContactSync
) : QkViewModel<ComposeView, ComposeState>(ComposeState(
        editingMode = threadId == 0L && address.isBlank(),
        selectedConversation = threadId,
        query = query)
) {

    private val attachments: Subject<List<Attachment>> = BehaviorSubject.createDefault(sharedAttachments)
    private val contacts: Observable<List<Contact>> by lazy { contactsRepo.getUnmanagedContacts().toObservable() }
    private val contactsReducer: Subject<(List<Contact>) -> List<Contact>> = PublishSubject.create()
    private val selectedContacts: Subject<List<Contact>> = BehaviorSubject.createDefault(listOf())
    private val threadIdSubject: Subject<Long> = BehaviorSubject.create()
    private val searchResults: Subject<List<Message>> = BehaviorSubject.create()
    private val searchSelection: Subject<Long> = BehaviorSubject.createDefault(-1)
    private val conversation: Subject<Conversation> = BehaviorSubject.create()
    private val messages: Subject<List<Message>> = BehaviorSubject.create()

    init {
        val initialConversation = threadId.takeIf { it != 0L }
                ?.let(conversationRepo::getConversationAsync)
                ?.asObservable()
                ?: Observable.empty()

        // Merges two potential conversation sources (threadId from constructor and contact selection) into a single
        // stream of conversations. If the conversation was deleted, notify the activity to shut down
        disposables += selectedContacts
                .skipWhile { it.isEmpty() }
                .map { contacts -> contacts.map { it.numbers.firstOrNull()?.address ?: "" } }
                .doOnNext { newState { copy(loading = true) } }
                .observeOn(Schedulers.computation())
                .map { addresses -> conversationRepo.getThreadId(addresses) ?: 0 }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { newState { copy(loading = false) } }
                .mergeWith(threadIdSubject)
                .switchMap { threadId ->
                    // Query the entire list of conversations and map from there, rather than
                    // directly querying the conversation from realm. If querying a single
                    // conversation and it doesn't exist yet, the realm query will never update
                    conversationRepo.getConversations().asObservable()
                            .map { conversations ->
                                conversations.firstOrNull { it.id == threadId }
                                        ?: conversationRepo.getOrCreateConversation(threadId)
                                        ?: Conversation(id = threadId)
                            }
                }
                .mergeWith(initialConversation)
                .filter { conversation -> conversation.isLoaded }
                .doOnNext { conversation ->
                    if (!conversation.isValid) {
                        newState { copy(hasError = true) }
                    }
                }
                .filter { conversation -> conversation.isValid }
                .subscribe(conversation::onNext)

        if (address.isNotBlank()) {
            selectedContacts.onNext(listOf(Contact(numbers = RealmList(PhoneNumber(address)))))
        }

        disposables += contactsReducer
                .scan(listOf<Contact>()) { previousState, reducer -> reducer(previousState) }
                .doOnNext { contacts -> newState { copy(selectedContacts = contacts) } }
                .skipUntil(state.filter { state -> state.editingMode })
                .takeUntil(state.filter { state -> !state.editingMode })
                .subscribe(selectedContacts::onNext)

        // When the conversation changes, mark read, and update the threadId and the messages for the adapter
        disposables += conversation
                .distinctUntilChanged { conversation -> conversation.id }
                .observeOn(AndroidSchedulers.mainThread())
                .map { conversation ->
                    val messages = messageRepo.getMessages(conversation.id)
                    newState { copy(selectedConversation = conversation.id, messages = Pair(conversation, messages)) }
                    messages
                }
                .switchMap { messages -> messages.asObservable() }
                .subscribe(messages::onNext)

        disposables += conversation
                .map { conversation -> conversation.getTitle() }
                .distinctUntilChanged()
                .subscribe { title -> newState { copy(conversationtitle = title) } }

        disposables += attachments
                .subscribe { attachments -> newState { copy(attachments = attachments) } }

        disposables += conversation
                .map { conversation -> conversation.id }
                .distinctUntilChanged()
                .withLatestFrom(state) { id, state -> messageRepo.getMessages(id, state.query) }
                .switchMap { messages -> messages.asObservable() }
                .takeUntil(state.map { it.query }.filter { it.isEmpty() })
                .filter { messages -> messages.isLoaded }
                .filter { messages -> messages.isValid }
                .subscribe(searchResults::onNext)

        disposables += Observables.combineLatest(searchSelection, searchResults) { selected, messages ->
            if (selected == -1L) {
                messages.lastOrNull()?.let { message -> searchSelection.onNext(message.id) }
            } else {
                val position = messages.indexOfFirst { it.id == selected } + 1
                newState { copy(searchSelectionPosition = position, searchResults = messages.size) }
            }
        }.subscribe()

        val latestSubId = messages
                .map { messages -> messages.lastOrNull()?.subId ?: -1 }
                .distinctUntilChanged()

        val subscriptions = ActiveSubscriptionObservable(subscriptionManager)
        disposables += Observables.combineLatest(latestSubId, subscriptions) { subId, subs ->
            val sub = if (subs.size > 1) subs.firstOrNull { it.subscriptionId == subId } ?: subs[0] else null
            newState { copy(subscription = sub) }
        }.subscribe()

        if (threadId == 0L) {
            syncContacts.execute(Unit)
        }
    }

    override fun bindView(view: ComposeView) {
        super.bindView(view)

        // Set the contact suggestions list to visible at all times when in editing mode and there are no contacts
        // selected yet, and also visible while in editing mode and there is text entered in the query field
        Observables
                .combineLatest(view.queryChangedIntent, selectedContacts) { query, selectedContacts ->
                    selectedContacts.isEmpty() || query.isNotEmpty()
                }
                .skipUntil(state.filter { state -> state.editingMode })
                .takeUntil(state.filter { state -> !state.editingMode })
                .distinctUntilChanged()
                .autoDisposable(view.scope())
                .subscribe { contactsVisible -> newState { copy(contactsVisible = contactsVisible && editingMode) } }

        // Update the list of contact suggestions based on the query input, while also filtering out any contacts
        // that have already been selected
        Observables
                .combineLatest(view.queryChangedIntent, contacts, selectedContacts) { query, contacts, selectedContacts ->

                    // Strip the accents from the query. This can be an expensive operation, so
                    // cache the result instead of doing it for each contact
                    val normalizedQuery = query.removeAccents()

                    var filteredContacts = contacts
                            .filterNot { contact -> selectedContacts.contains(contact) }
                            .filter { contact -> contactFilter.filter(contact, normalizedQuery) }

                    // If the entry is a valid destination, allow it as a recipient
                    if (PhoneNumberUtils.isWellFormedSmsAddress(query.toString())) {
                        val newAddress = PhoneNumberUtils.formatNumber(query.toString(), Locale.getDefault().country)
                        val newContact = Contact(numbers = RealmList(PhoneNumber(address = newAddress
                                ?: query.toString())))
                        filteredContacts = listOf(newContact) + filteredContacts
                    }

                    filteredContacts
                }
                .skipUntil(state.filter { state -> state.editingMode })
                .takeUntil(state.filter { state -> !state.editingMode })
                .subscribeOn(Schedulers.computation())
                .autoDisposable(view.scope())
                .subscribe { contacts -> newState { copy(contacts = contacts) } }

        // Backspaces should delete the most recent contact if there's no text input
        // Close the activity if user presses back
        view.queryBackspaceIntent
                .withLatestFrom(selectedContacts, view.queryChangedIntent) { event, contacts, query ->
                    if (contacts.isNotEmpty() && query.isEmpty()) {
                        contactsReducer.onNext { it.dropLast(1) }
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        // Enter the first contact suggestion if the enter button is pressed
        view.queryEditorActionIntent
                .filter { actionId -> actionId == EditorInfo.IME_ACTION_DONE }
                .withLatestFrom(state) { _, state -> state }
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
                .subscribe { newState { copy() } }

        // Open the phone dialer if the call button is clicked
        view.optionsItemIntent
                .filter { it == R.id.call }
                .withLatestFrom(conversation) { _, conversation -> conversation }
                .mapNotNull { conversation -> conversation.recipients.firstOrNull() }
                .map { recipient -> recipient.address }
                .autoDisposable(view.scope())
                .subscribe { address -> navigator.makePhoneCall(address) }

        // Open the conversation settings if info button is clicked
        view.optionsItemIntent
                .filter { it == R.id.info }
                .withLatestFrom(conversation) { _, conversation -> conversation }
                .autoDisposable(view.scope())
                .subscribe { conversation -> navigator.showConversationInfo(conversation.id) }

        // Copy the message contents
        view.optionsItemIntent
                .filter { it == R.id.copy }
                .withLatestFrom(view.messagesSelectedIntent) { _, messages ->
                    messages?.firstOrNull()?.let { messageRepo.getMessage(it) }?.let { message ->
                        ClipboardUtils.copy(context, message.getText())
                        context.makeToast(R.string.toast_copied)
                    }
                }
                .autoDisposable(view.scope())
                .subscribe { view.clearSelection() }

        // Show the message details
        view.optionsItemIntent
                .filter { it == R.id.details }
                .withLatestFrom(view.messagesSelectedIntent) { _, messages -> messages }
                .mapNotNull { messages -> messages.firstOrNull().also { view.clearSelection() } }
                .mapNotNull(messageRepo::getMessage)
                .map(messageDetailsFormatter::format)
                .autoDisposable(view.scope())
                .subscribe { view.showDetails(it) }

        // Delete the messages
        view.optionsItemIntent
                .filter { it == R.id.delete }
                .withLatestFrom(view.messagesSelectedIntent, conversation) { _, messages, conversation ->
                    deleteMessages.execute(DeleteMessages.Params(messages, conversation.id))
                }
                .autoDisposable(view.scope())
                .subscribe { view.clearSelection() }

        // Forward the message
        view.optionsItemIntent
                .filter { it == R.id.forward }
                .withLatestFrom(view.messagesSelectedIntent) { _, messages ->
                    messages?.firstOrNull()?.let { messageRepo.getMessage(it) }?.let { message ->
                        val images = message.parts.filter { it.isImage() }.mapNotNull { it.getUri() }
                        navigator.showCompose(message.body, images)
                    }
                }
                .autoDisposable(view.scope())
                .subscribe { view.clearSelection() }


        // Show the previous search result
        view.optionsItemIntent
                .filter { it == R.id.previous }
                .withLatestFrom(searchSelection, searchResults) { _, selection, messages ->
                    val currentPosition = messages.indexOfFirst { it.id == selection }
                    if (currentPosition <= 0L) messages.lastOrNull()?.id ?: -1
                    else messages.getOrNull(currentPosition - 1)?.id ?: -1
                }
                .filter { id -> id != -1L }
                .autoDisposable(view.scope())
                .subscribe(searchSelection)


        // Show the next search result
        view.optionsItemIntent
                .filter { it == R.id.next }
                .withLatestFrom(searchSelection, searchResults) { _, selection, messages ->
                    val currentPosition = messages.indexOfFirst { it.id == selection }
                    if (currentPosition >= messages.size - 1) messages.firstOrNull()?.id ?: -1
                    else messages.getOrNull(currentPosition + 1)?.id ?: -1
                }
                .filter { id -> id != -1L }
                .autoDisposable(view.scope())
                .subscribe(searchSelection)


        // Clear the search
        view.optionsItemIntent
                .filter { it == R.id.clear }
                .autoDisposable(view.scope())
                .subscribe { newState { copy(query = "", searchSelectionId = -1) } }


        // Toggle the group sending mode
        view.sendAsGroupIntent
                .autoDisposable(view.scope())
                .subscribe { newState { copy(sendAsGroup = !sendAsGroup) } }


        // Scroll to search position
        searchSelection
                .filter { id -> id != -1L }
                .doOnNext { id -> newState { copy(searchSelectionId = id) } }
                .autoDisposable(view.scope())
                .subscribe(view::scrollToMessage)


        // Retry sending
        view.messageClickIntent
                .filter { message -> message.isFailedMessage() }
                .doOnNext { message -> retrySending.execute(message) }
                .autoDisposable(view.scope())
                .subscribe()

        // Update the State when the message selected count changes
        view.messagesSelectedIntent
                .map { selection -> selection.size }
                .autoDisposable(view.scope())
                .subscribe { messages -> newState { copy(selectedMessages = messages, editingMode = false) } }

        // Cancel sending a message
        view.cancelSendingIntent
                .doOnNext { message -> view.setDraft(message.getText()) }
                .autoDisposable(view.scope())
                .subscribe { message -> cancelMessage.execute(message.id) }

        // Set the current conversation
        Observables
                .combineLatest(
                        view.activityVisibleIntent.distinctUntilChanged(),
                        conversation.mapNotNull { conversation -> conversation.takeIf { it.isValid }?.id }.distinctUntilChanged())
                { visible, threadId ->
                    when (visible) {
                        true -> {
                            activeConversationManager.setActiveConversation(threadId)
                            markRead.execute(listOf(threadId))
                        }

                        false -> activeConversationManager.setActiveConversation(null)
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        // Save draft when the activity goes into the background
        view.activityVisibleIntent
                .filter { visible -> !visible }
                .withLatestFrom(conversation) { _, conversation -> conversation }
                .mapNotNull { conversation -> conversation.takeIf { it.isValid }?.id }
                .observeOn(Schedulers.io())
                .withLatestFrom(view.textChangedIntent) { threadId, draft ->
                    conversationRepo.saveDraft(threadId, draft.toString())
                }
                .autoDisposable(view.scope())
                .subscribe()

        // Open the attachment options
        view.attachIntent
                .autoDisposable(view.scope())
                .subscribe { newState { copy(attaching = !attaching) } }

        // Attach a photo from camera
        view.cameraIntent
                .autoDisposable(view.scope())
                .subscribe {
                    if (permissionManager.hasStorage()) {
                        newState { copy(attaching = false) }
                        view.requestCamera()
                    } else {
                        view.requestStoragePermission()
                    }
                }

        // Attach a photo from gallery
        view.galleryIntent
                .doOnNext { newState { copy(attaching = false) } }
                .autoDisposable(view.scope())
                .subscribe { view.requestGallery() }

        // Choose a time to schedule the message
        view.scheduleIntent
                .doOnNext { newState { copy(attaching = false) } }
                .withLatestFrom(billingManager.upgradeStatus) { _, upgraded -> upgraded }
                .filter { upgraded ->
                    upgraded.also { if (!upgraded) view.showQksmsPlusSnackbar(R.string.compose_scheduled_plus) }
                }
                .autoDisposable(view.scope())
                .subscribe { view.requestDatePicker() }

        // A photo was selected
        Observable.merge(
                view.attachmentSelectedIntent.map { uri -> Attachment(uri) },
                view.inputContentIntent.map { inputContent -> Attachment(inputContent = inputContent) })
                .withLatestFrom(attachments) { attachment, attachments -> attachments + attachment }
                .doOnNext { attachments.onNext(it) }
                .autoDisposable(view.scope())
                .subscribe { newState { copy(attaching = false) } }

        // Set the scheduled time
        view.scheduleSelectedIntent
                .filter { scheduled ->
                    (scheduled > System.currentTimeMillis()).also { future ->
                        if (!future) context.toast(R.string.compose_scheduled_future)
                    }
                }
                .autoDisposable(view.scope())
                .subscribe { scheduled -> newState { copy(scheduled = scheduled) } }

        // Detach a photo
        view.attachmentDeletedIntent
                .withLatestFrom(attachments) { bitmap, attachments -> attachments.filter { it !== bitmap } }
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
                .combineLatest(view.textChangedIntent, attachments) { text, attachments ->
                    text.isNotBlank() || attachments.isNotEmpty()
                }
                .autoDisposable(view.scope())
                .subscribe { canSend -> newState { copy(canSend = canSend) } }

        // Show the remaining character counter when necessary
        view.textChangedIntent
                .observeOn(Schedulers.computation())
                .mapNotNull { draft -> tryOrNull { SmsMessage.calculateLength(draft, prefs.unicode.get()) } }
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
                .subscribe { remaining -> newState { copy(remaining = remaining) } }

        // Cancel the scheduled time
        view.scheduleCancelIntent
                .autoDisposable(view.scope())
                .subscribe { newState { copy(scheduled = 0) } }

        // Toggle to the next sim slot
        view.changeSimIntent
                .withLatestFrom(state) { _, state ->
                    val subs = subscriptionManager.activeSubscriptionInfoList
                    val subIndex = subs.indexOfFirst { it.subscriptionId == state.subscription?.subscriptionId }
                    val subscription = when {
                        subIndex == -1 -> null
                        subIndex < subs.size - 1 -> subs[subIndex + 1]
                        else -> subs[0]
                    }
                    newState { copy(subscription = subscription) }
                }
                .autoDisposable(view.scope())
                .subscribe()

        // Send a message when the send button is clicked, and disable editing mode if it's enabled
        view.sendIntent
                .filter {
                    val hasPermission = permissionManager.hasSendSms()
                    if (!hasPermission) view.requestSmsPermission()
                    hasPermission
                }
                .withLatestFrom(view.textChangedIntent) { _, body -> body }
                .map { body -> body.toString() }
                .withLatestFrom(state, attachments, conversation, selectedContacts) { body, state, attachments, conversation, contacts ->
                    val subId = state.subscription?.subscriptionId ?: -1
                    val addresses = when (conversation.recipients.isNotEmpty()) {
                        true -> conversation.recipients.map { it.address }
                        false -> contacts.mapNotNull { it.numbers.firstOrNull()?.address }
                    }
                    val delay = when (prefs.sendDelay.get()) {
                        Preferences.SEND_DELAY_SHORT -> 3000
                        Preferences.SEND_DELAY_MEDIUM -> 5000
                        Preferences.SEND_DELAY_LONG -> 10000
                        else -> 0
                    }

                    when {
                        state.scheduled != 0L -> {
                            newState { copy(scheduled = 0) }
                            val uris = attachments.map { it.getUri() }.map { it.toString() }
                            val params = AddScheduledMessage.Params(state.scheduled, subId, addresses, state.sendAsGroup, body, uris)
                            addScheduledMessage.execute(params)
                            context.toast(R.string.compose_scheduled_toast)
                        }

                        state.sendAsGroup -> {
                            val threadId = conversation.id.takeIf { it > 0 }
                                    ?: TelephonyCompat.getOrCreateThreadId(context, addresses).also(threadIdSubject::onNext)
                            sendMessage.execute(SendMessage.Params(subId, threadId, addresses, body, attachments, delay))
                        }

                        else -> {
                            addresses.forEach { addr ->
                                val threadId = TelephonyCompat.getOrCreateThreadId(context, addr)
                                val address = listOf(conversationRepo.getConversation(threadId)?.recipients?.firstOrNull()?.address
                                        ?: addr)
                                sendMessage.execute(SendMessage.Params(subId, threadId, address, body, attachments, delay))
                            }
                        }
                    }

                    view.setDraft("")
                    this.attachments.onNext(ArrayList())

                    if (state.editingMode) {
                        newState { copy(editingMode = false, sendAsGroup = true, hasError = !state.sendAsGroup) }
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        // View QKSMS+
        view.viewQksmsPlusIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showQksmsPlusActivity("compose_schedule") }

        // Navigate back
        view.optionsItemIntent
                .filter { it == android.R.id.home }
                .map { Unit }
                .mergeWith(view.backPressedIntent)
                .withLatestFrom(state) { _, state ->
                    when {
                        state.selectedMessages > 0 -> view.clearSelection()
                        else -> newState { copy(hasError = true) }
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

    }

}