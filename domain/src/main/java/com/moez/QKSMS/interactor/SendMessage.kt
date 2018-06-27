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
package com.moez.QKSMS.interactor

import com.moez.QKSMS.model.Attachment
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class SendMessage @Inject constructor(
        private val conversationRepo: ConversationRepository,
        private val messageRepo: MessageRepository
) : Interactor<SendMessage.Params>() {

    data class Params(
            val subId: Int,
            val threadId: Long,
            val addresses: List<String>,
            val body: String,
            val attachments: List<Attachment> = listOf(),
            val delay: Int = 0)

    override fun buildObservable(params: Params): Flowable<Unit> = Flowable.just(Unit)
            .filter { params.addresses.isNotEmpty() }
            .doOnNext { messageRepo.sendMessage(params.subId, params.threadId, params.addresses, params.body, params.attachments, params.delay) }
            // If this was the first message sent in the conversation, the conversation might not exist yet
            .doOnNext { conversationRepo.getOrCreateConversation(params.threadId) }
            .doOnNext { conversationRepo.updateConversations(params.threadId) }
            .doOnNext { conversationRepo.markUnarchived(params.threadId) }

}