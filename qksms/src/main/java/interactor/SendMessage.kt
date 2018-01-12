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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import common.util.Preferences
import data.model.Message
import data.repository.MessageRepository
import io.reactivex.Flowable
import presentation.receiver.MessageDeliveredReceiver
import presentation.receiver.MessageSentReceiver
import javax.inject.Inject

class SendMessage @Inject constructor(
        private val context: Context,
        private val prefs: Preferences,
        private val messageRepo: MessageRepository)
    : Interactor<SendMessage.Params, Message>() {

    data class Params(val threadId: Long, val address: String, val body: String)

    override fun buildObservable(params: Params): Flowable<Message> {
        val smsManager = SmsManager.getDefault()

        return Flowable.just(params)
                .map { messageRepo.insertSentSms(params.threadId, params.address, params.body) }
                .doOnNext { message ->
                    val parts = smsManager.divideMessage(params.body)

                    val sentIntents = parts.map {
                        val intent = Intent(context, MessageSentReceiver::class.java).putExtra("id", message.id)
                        PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    }

                    val deliveredIntents = parts.map {
                        val intent = Intent(context, MessageDeliveredReceiver::class.java).putExtra("id", message.id)
                        val pendingIntent = PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        if (prefs.delivery.get()) pendingIntent else null
                    }

                    smsManager.sendMultipartTextMessage(params.address, null, parts, ArrayList(sentIntents), ArrayList(deliveredIntents))
                }
    }

}