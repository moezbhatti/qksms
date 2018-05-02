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
import io.reactivex.Flowable
import manager.ExternalBlockingManager
import manager.NotificationManager
import repository.MessageRepository
import util.extensions.mapNotNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReceiveSms @Inject constructor(
        private val externalBlockingManager: ExternalBlockingManager,
        private val messageRepo: MessageRepository,
        private val notificationManager: NotificationManager,
        private val updateBadge: UpdateBadge
) : Interactor<Array<SmsMessage>>() {

    override fun buildObservable(params: Array<SmsMessage>): Flowable<*> {
        return Flowable.just(params)
                .filter { it.isNotEmpty() }
                .filter { messages -> // Don't continue if the sender is blocked
                    val address = messages[0].displayOriginatingAddress
                    !externalBlockingManager.shouldBlock(address).blockingGet()
                }
                .map { messages ->
                    val address = messages[0].displayOriginatingAddress
                    val time = messages[0].timestampMillis
                    val body: String = messages
                            .map { message -> message.displayMessageBody }
                            .reduce { body, new -> body + new }

                    messageRepo.insertReceivedSms(address, body, time) // Add the message to the db
                }
                .doOnNext { message -> messageRepo.updateConversations(message.threadId) } // Update the conversation
                .mapNotNull { message -> messageRepo.getOrCreateConversation(message.threadId) } // Map message to conversation
                .filter { conversation -> !conversation.blocked } // Don't notify for blocked conversations
                .doOnNext { conversation -> if (conversation.archived) messageRepo.markUnarchived(conversation.id) } // Unarchive conversation if necessary
                .map { conversation -> conversation.id } // Map to the id because [delay] will put us on the wrong thread
                .delay(1, TimeUnit.SECONDS) // Wait one second before trying to notify, in case the foreground app marks it as read first
                .doOnNext { threadId -> notificationManager.update(threadId) } // Update the notification
                .flatMap { updateBadge.buildObservable(Unit) } // Update the badge
    }

}