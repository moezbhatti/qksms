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
package interactor

import android.telephony.SmsMessage
import common.util.NotificationManager
import common.util.extensions.mapNotNull
import data.model.Conversation
import data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class ReceiveSms @Inject constructor(
        private val messageRepo: MessageRepository,
        private val notificationManager: NotificationManager)
    : Interactor<Array<SmsMessage>, Conversation>() {

    override fun buildObservable(params: Array<SmsMessage>): Flowable<Conversation> {
        return Flowable.just(params)
                .filter { it.isNotEmpty() }
                .map { messages ->
                    val address = messages[0].displayOriginatingAddress
                    val time = messages[0].timestampMillis
                    val body: String = messages
                            .map { message -> message.displayMessageBody }
                            .reduce { body, new -> body + new }

                    messageRepo.insertReceivedSms(address, body, time) // Add the message to the db
                }
                .mapNotNull { message -> messageRepo.getOrCreateConversation(message.threadId) } // Map message to conversation
                .doOnNext { conversation -> if (conversation.archived) messageRepo.markUnarchived(conversation.id) } // Unarchive conversation if necessary
                .doOnNext { conversation -> notificationManager.update(conversation.id) } // Update the notification
    }

}