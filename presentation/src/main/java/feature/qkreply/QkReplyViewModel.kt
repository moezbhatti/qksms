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
import common.base.QkViewModel
import injection.appComponent
import io.reactivex.rxkotlin.plusAssign
import model.Conversation
import repository.MessageRepository
import util.extensions.asObservable
import javax.inject.Inject

class QkReplyViewModel(intent: Intent) : QkViewModel<QkReplyView, QkReplyState>(QkReplyState()) {

    @Inject lateinit var messageRepo: MessageRepository

    init {
        appComponent.inject(this)

        val threadId = intent.getLongExtra("threadId", -1)

        disposables += messageRepo.getConversationAsync(threadId)
                .asObservable<Conversation>()
                .filter { it.isLoaded }
                .filter { it.isValid }
                .distinctUntilChanged()
                .doOnNext { conversation -> newState { it.copy(title = conversation.getTitle()) } }
                .map { conversation -> Pair(conversation, messageRepo.getUnreadMessages(conversation.id)) }
                .doOnNext { data -> newState { it.copy(data = data) } }
                .subscribe()
    }

}
