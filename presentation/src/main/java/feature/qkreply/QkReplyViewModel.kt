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
package feature.qkreply

import android.content.Intent
import android.telephony.SmsMessage
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.Navigator
import common.base.QkViewModel
import injection.appComponent
import interactor.MarkRead
import interactor.SendMessage
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import model.Conversation
import repository.MessageRepository
import util.extensions.asObservable
import util.extensions.mapNotNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class QkReplyViewModel(intent: Intent) : QkViewModel<QkReplyView, QkReplyState>(QkReplyState()) {

    @Inject lateinit var markRead: MarkRead
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var sendMessage: SendMessage

    private val conversation by lazy {
        messageRepo.getConversationAsync(intent.getLongExtra("threadId", -1))
                .asObservable<Conversation>()
                .filter { it.isLoaded }
                .filter { it.isValid }
                .distinctUntilChanged()
    }

    init {
        appComponent.inject(this)

        disposables += markRead
        disposables += sendMessage

        disposables += conversation
                .doOnNext { conversation -> newState { it.copy(title = conversation.getTitle()) } }
                .distinctUntilChanged { conversation -> conversation.id } // We only need to set the messages once
                .map { conversation -> Pair(conversation, messageRepo.getUnreadMessages(conversation.id)) }
                .subscribe { data -> newState { it.copy(data = data) } }
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
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .map { conversation -> conversation.id }
                .autoDisposable(view.scope())
                .subscribe { threadId -> markRead.execute(threadId) { view.finish() } }

        // Call
        view.menuItemIntent
                .filter { id -> id == R.id.call }
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .mapNotNull { conversation -> conversation.recipients.first()?.address }
                .doOnNext { address -> navigator.makePhoneCall(address) }
                .autoDisposable(view.scope())
                .subscribe { view.finish() }

        // Show all messages
        view.menuItemIntent
                .filter { id -> id == R.id.expand }
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .map { conversation -> Pair(conversation, messageRepo.getMessages(conversation.id)) }
                .autoDisposable(view.scope())
                .subscribe { data -> newState { it.copy(expanded = true, data = data) } }

        // Show unread messages only
        view.menuItemIntent
                .filter { id -> id == R.id.collapse }
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .map { conversation -> Pair(conversation, messageRepo.getUnreadMessages(conversation.id)) }
                .autoDisposable(view.scope())
                .subscribe { data -> newState { it.copy(expanded = false, data = data) } }

        // View conversation
        view.menuItemIntent
                .filter { id -> id == R.id.view }
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .map { conversation -> conversation.id }
                .doOnNext { threadId -> navigator.showConversation(threadId) }
                .autoDisposable(view.scope())
                .subscribe { view.finish() }

        // Enable the send button when there is text input into the new message body or there's
        // an attachment, disable otherwise
        view.textChangedIntent
                .map { text -> text.isNotBlank() }
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
                .withLatestFrom(conversation, { body, conversation ->
                    val threadId = conversation.id
                    val addresses = conversation.recipients.map { it.address }

                    view.setDraft("")
                    sendMessage.execute(SendMessage.Params(threadId, addresses, body, listOf())) {
                        markRead.execute(threadId) {
                            view.finish()
                        }
                    }
                })
                .autoDisposable(view.scope())
                .subscribe()
    }

}
