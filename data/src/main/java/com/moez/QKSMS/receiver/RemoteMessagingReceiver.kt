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
package com.moez.QKSMS.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.moez.QKSMS.compat.SubscriptionManagerCompat
import com.moez.QKSMS.interactor.MarkRead
import com.moez.QKSMS.interactor.SendMessage
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.MessageRepository
import dagger.android.AndroidInjection
import javax.inject.Inject

class RemoteMessagingReceiver : BroadcastReceiver() {

    @Inject lateinit var conversationRepo: ConversationRepository
    @Inject lateinit var markRead: MarkRead
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var sendMessage: SendMessage
    @Inject lateinit var subscriptionManager: SubscriptionManagerCompat

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        val remoteInput = RemoteInput.getResultsFromIntent(intent) ?: return
        val bundle = intent.extras ?: return

        val threadId = bundle.getLong("threadId")
        val body = remoteInput.getCharSequence("body").toString()
        markRead.execute(listOf(threadId))

        val lastMessage = messageRepo.getMessages(threadId).lastOrNull()
        val subId = subscriptionManager.activeSubscriptionInfoList.firstOrNull { it.subscriptionId == lastMessage?.subId }?.subscriptionId ?: -1
        val addresses = conversationRepo.getConversation(threadId)?.recipients?.map { it.address } ?: return

        val pendingRepository = goAsync()
        sendMessage.execute(SendMessage.Params(subId, threadId, addresses, body)) { pendingRepository.finish() }
    }
}
