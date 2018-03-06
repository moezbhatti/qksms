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

import android.net.Uri
import common.util.NotificationManager
import common.util.SyncManager
import common.util.extensions.mapNotNull
import data.model.Conversation
import data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class ReceiveMms @Inject constructor(
        private val syncManager: SyncManager,
        private val messageRepo: MessageRepository,
        private val notificationManager: NotificationManager
) : Interactor<Uri>() {

    override fun buildObservable(params: Uri): Flowable<Conversation> {
        return Flowable.just(params)
                .flatMap { uri -> syncManager.syncMessage(uri) } // Sync the message
                .mapNotNull { message -> messageRepo.getOrCreateConversation(message.threadId) } // Map message to conversation
                .filter { conversation -> !conversation.blocked } // Don't notify for blocked conversations
                .doOnNext { conversation -> if (conversation.archived) messageRepo.markUnarchived(conversation.id) } // Unarchive conversation if necessary
                .doOnNext { conversation -> notificationManager.update(conversation.id) } // Update the notification
    }

}