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
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.base.QkViewModel
import injection.appComponent
import interactor.MarkRead
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import model.Conversation
import repository.MessageRepository
import util.extensions.asObservable
import javax.inject.Inject

class QkReplyViewModel(intent: Intent) : QkViewModel<QkReplyView, QkReplyState>(QkReplyState()) {

    @Inject lateinit var markRead: MarkRead
    @Inject lateinit var messageRepo: MessageRepository

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

        disposables += conversation
                .doOnNext { conversation -> newState { it.copy(title = conversation.getTitle()) } }
                .take(1) // We only need to set the messages once
                .map { conversation -> Pair(conversation, messageRepo.getUnreadMessages(conversation.id)) }
                .subscribe { data -> newState { it.copy(data = data) } }
    }

    override fun bindView(view: QkReplyView) {
        super.bindView(view)

        // Mark read
        view.menuItemIntent
                .filter { id -> id == R.id.read }
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .map { conversation -> conversation.id }
                .autoDisposable(view.scope())
                .subscribe { threadId -> markRead.execute(threadId) }

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
    }

}
