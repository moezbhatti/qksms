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
import android.net.Uri
import android.telephony.SmsManager
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import com.mlsdev.rximagepicker.RxImageConverters
import common.util.Preferences
import data.repository.MessageRepository
import id.zelory.compressor.Compressor
import io.reactivex.Flowable
import presentation.receiver.MessageDeliveredReceiver
import presentation.receiver.MessageSentReceiver
import java.io.File
import javax.inject.Inject

class SendMessage @Inject constructor(
        private val context: Context,
        private val prefs: Preferences,
        private val messageRepo: MessageRepository
) : Interactor<SendMessage.Params, Unit>() {

    data class Params(val threadId: Long, val addresses: List<String>, val body: String, val attachments: List<Uri> = listOf())

    override fun buildObservable(params: Params): Flowable<Unit> {
        return Flowable.just(Unit)
                .filter { params.addresses.isNotEmpty() }
                .doOnNext {
                    if (params.addresses.size == 1 && params.attachments.isEmpty()) {
                        sendSms(params.threadId, params.addresses.first(), params.body)
                    } else {
                        sendMms(params.threadId, params.addresses, params.body, params.attachments)
                    }
                }
    }

    private fun sendSms(threadId: Long, address: String, body: String) {
        val smsManager = SmsManager.getDefault()

        val message = messageRepo.insertSentSms(threadId, address, body)
        val parts = smsManager.divideMessage(body)

        val sentIntents = parts.map {
            val intent = Intent(context, MessageSentReceiver::class.java).putExtra("id", message.id)
            PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val deliveredIntents = parts.map {
            val intent = Intent(context, MessageDeliveredReceiver::class.java).putExtra("id", message.id)
            val pendingIntent = PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
            if (prefs.delivery.get()) pendingIntent else null
        }

        smsManager.sendMultipartTextMessage(address, null, parts, ArrayList(sentIntents), ArrayList(deliveredIntents))
    }

    private fun sendMms(threadId: Long, addresses: List<String>, body: String, attachments: List<Uri>) {
        val settings = Settings().apply {
        }


        val message = Message(body, addresses.toTypedArray())
        attachments
                .map { uri ->
                    val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpeg")
                    RxImageConverters.uriToFile(context, uri, file).blockingFirst()
                }
                .map { file ->
                    Compressor(context)
                            .setMaxHeight(1920)
                            .setMaxHeight(1920)
                            .setQuality(60)
                            .compressToBitmap(file)
                }
                .forEach { bitmap ->
                    message.addImage(bitmap)
                }

        val transaction = Transaction(context, settings)
        transaction.sendNewMessage(message, threadId)
    }

}