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
package com.moez.QKSMS.feature.qkreply

import android.telephony.SmsMessage
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkViewModel
import com.moez.QKSMS.compat.SubscriptionManagerCompat
import com.moez.QKSMS.extensions.asObservable
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.interactor.DeleteMessages
import com.moez.QKSMS.interactor.MarkRead
import com.moez.QKSMS.interactor.SendMessage
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.MessageRepository
import com.moez.QKSMS.util.ActiveSubscriptionObservable
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class QkReplyViewModel @Inject constructor(
        @Named("threadId") private val threadId: Long,
        private val conversationRepo: ConversationRepository,
        private val deleteMessages: DeleteMessages,
        private val markRead: MarkRead,
        private val messageRepo: MessageRepository,
        private val navigator: Navigator,
        private val sendMessage: SendMessage,
        private val subscriptionManager: SubscriptionManagerCompat
) : QkViewModel<QkReplyView, QkReplyState>(QkReplyState(selectedConversation = threadId)) {

    private val conversation by lazy {
        conversationRepo.getConversationAsync(threadId)
                .asObservable()
                .filter { it.isLoaded }
                .filter { it.isValid }
                .distinctUntilChanged()
    }

    private val messages: Subject<RealmResults<Message>> =
            BehaviorSubject.createDefault(messageRepo.getUnreadMessages(threadId))

    init {
        disposables += markRead
        disposables += sendMessage

        // When the set of messages changes, update the state
        // If we're ever showing an empty set of messages, then it's time to shut down to activity
        disposables += Observables
                .combineLatest(messages, conversation) { messages, conversation ->
                    newState { copy(data = Pair(conversation, messages)) }
                    messages
                }
                .switchMap { messages -> messages.asObservable() }
                .filter { it.isLoaded }
                .filter { it.isValid }
                .filter { it.isEmpty() }
                .subscribe { newState { copy(hasError = true) } }

        disposables += conversation
                .map { conversation -> conversation.getTitle() }
                .distinctUntilChanged()
                .subscribe { title -> newState { copy(title = title) } }

        val latestSubId = messages
                .map { messages -> messages.lastOrNull()?.subId ?: -1 }
                .distinctUntilChanged()

        val subscriptions = ActiveSubscriptionObservable(subscriptionManager)
        disposables += Observables.combineLatest(latestSubId, subscriptions) { subId, subs ->
            val sub = if (subs.size > 1) subs.firstOrNull { it.subscriptionId == subId } ?: subs[0] else null
            newState { copy(subscription = sub) }
        }.subscribe()
    }

    override fun bindView(view: QkReplyView) {
        super.bindView(view)

        conversation
                .map { conversation -> conversation.draft }
                .distinctUntilChanged()
                .autoDisposable(view.scope())
                .subscribe { draft -> view.setDraft(draft) }

        // Mark read
        view.menuItemIntent
                .filter { id -> id == R.id.read }
                .autoDisposable(view.scope())
                .subscribe {
                    markRead.execute(listOf(threadId)) { newState { copy(hasError = true) } }
                }

        // Call
        view.menuItemIntent
                .filter { id -> id == R.id.call }
                .withLatestFrom(conversation) { _, conversation -> conversation }
                .mapNotNull { conversation -> conversation.recipients.first()?.address }
                .doOnNext { address -> navigator.makePhoneCall(address) }
                .autoDisposable(view.scope())
                .subscribe { newState { copy(hasError = true) } }

        // Show all messages
        view.menuItemIntent
                .filter { id -> id == R.id.expand }
                .map { messageRepo.getMessages(threadId) }
                .doOnNext(messages::onNext)
                .autoDisposable(view.scope())
                .subscribe { newState { copy(expanded = true) } }

        // Show unread messages only
        view.menuItemIntent
                .filter { id -> id == R.id.collapse }
                .map { messageRepo.getUnreadMessages(threadId) }
                .doOnNext(messages::onNext)
                .autoDisposable(view.scope())
                .subscribe { newState { copy(expanded = false) } }

        // Delete new messages
        view.menuItemIntent
                .filter { id -> id == R.id.delete }
                .observeOn(Schedulers.io())
                .map { messageRepo.getUnreadMessages(threadId).map { it.id } }
                .map { messages -> DeleteMessages.Params(messages, threadId) }
                .autoDisposable(view.scope())
                .subscribe { deleteMessages.execute(it) { newState { copy(hasError = true) } } }

        // View conversation
        view.menuItemIntent
                .filter { id -> id == R.id.view }
                .doOnNext { navigator.showConversation(threadId) }
                .autoDisposable(view.scope())
                .subscribe { newState { copy(hasError = true) } }

        // Enable the send button when there is text input into the new message body or there's
        // an attachment, disable otherwise
        view.textChangedIntent
                .map { text -> text.isNotBlank() }
                .autoDisposable(view.scope())
                .subscribe { canSend -> newState { copy(canSend = canSend) } }

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
                .subscribe { remaining -> newState { copy(remaining = remaining) } }

        // Update the draft whenever the text is changed
        view.textChangedIntent
                .debounce(100, TimeUnit.MILLISECONDS)
                .map { draft -> draft.toString() }
                .observeOn(Schedulers.io())
                .autoDisposable(view.scope())
                .subscribe { draft -> conversationRepo.saveDraft(threadId, draft) }

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
                .withLatestFrom(view.textChangedIntent) { _, body -> body }
                .map { body -> body.toString() }
                .withLatestFrom(state, conversation) { body, state, conversation ->
                    val subId = state.subscription?.subscriptionId ?: -1
                    val addresses = conversation.recipients.map { it.address }
                    sendMessage.execute(SendMessage.Params(subId, threadId, addresses, body))
                    view.setDraft("")
                }
                .doOnNext {
                    markRead.execute(listOf(threadId)) { newState { copy(hasError = true) } }
                }
                .autoDisposable(view.scope())
                .subscribe()
    }

}
