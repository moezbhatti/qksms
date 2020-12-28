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

import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.MessageRepository
import com.moez.QKSMS.util.Preferences
import io.reactivex.Flowable
import timber.log.Timber
import javax.inject.Inject

class DeleteOldMessages @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val prefs: Preferences
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit): Flowable<*> = Flowable.fromCallable {
        val maxAge = prefs.autoDelete.get().takeIf { it > 0 } ?: return@fromCallable
        val counts = messageRepo.getOldMessageCounts(maxAge)
        val threadIds = counts.keys.toLongArray()

        Timber.d("Deleting ${counts.values.sum()} old messages from ${threadIds.size} conversations")
        messageRepo.deleteOldMessages(maxAge)
        conversationRepo.updateConversations(*threadIds)
    }

}
