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
package com.moez.QKSMS.domain.interactor

import com.moez.QKSMS.common.util.NotificationManager
import com.moez.QKSMS.common.util.extensions.mapNotNull
import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class ReceiveMessage @Inject constructor(
        private val messageRepo: MessageRepository,
        private val notificationManager: NotificationManager)
    : Interactor<ReceiveMessage.Params, Unit>() {

    data class Params(val address: String, val body: String, val sentTime: Long)

    override fun buildObservable(params: Params): Flowable<Unit> {
        return Flowable.just(params)
                .map { messageRepo.insertReceivedSms(params.address, params.body, params.sentTime) } // Add the message to the db
                .mapNotNull { message -> messageRepo.getConversation(message.threadId) } // Map message to conversation
                .doOnNext { conversation -> if (conversation.archived) messageRepo.markUnarchived(conversation.id) } // Unarchive conversation if necessary
                .doOnNext { conversation -> notificationManager.update(conversation.id) } // Update the notification
                .map { }
    }

}