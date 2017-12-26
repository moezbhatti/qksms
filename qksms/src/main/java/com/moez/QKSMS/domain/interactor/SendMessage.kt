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

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import com.moez.QKSMS.common.util.Preferences
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.receiver.MessageDeliveredReceiver
import com.moez.QKSMS.receiver.MessageSentReceiver
import io.reactivex.Flowable
import io.reactivex.rxkotlin.toFlowable
import javax.inject.Inject

class SendMessage @Inject constructor(
        private val context: Context,
        private val prefs: Preferences,
        private val messageRepo: MessageRepository)
    : Interactor<SendMessage.Params, Unit>() {

    data class Params(val threadId: Long, val address: String, val body: String)

    override fun buildObservable(params: Params): Flowable<Unit> {

        val contentResolver = context.contentResolver
        val smsManager = SmsManager.getDefault()

        return Flowable.just(prefs.split.get())
                .map { split ->
                    when (split) {
                        true -> smsManager.divideMessage(params.body)
                        false -> arrayListOf(params.body)
                    }
                }
                .flatMap { bodies -> bodies.toFlowable() }
                .map { bodies -> createContentValues(params.threadId, params.address, bodies) }
                .map { values -> Pair(values, contentResolver.insert(Telephony.Sms.CONTENT_URI, values)) }
                .doOnNext { (_, uri) -> messageRepo.addMessageFromUri(uri) }
                .map { (values, uri) ->
                    val sentIntent = Intent(context, MessageSentReceiver::class.java).putExtra("uri", uri.toString())
                    val sentPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), sentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val deliveredIntent = Intent(context, MessageDeliveredReceiver::class.java).putExtra("uri", uri.toString())
                    val deliveredPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    Triple(values.getAsString(Telephony.Sms.BODY), sentPI, deliveredPI)
                }
                .toList().toFlowable()
                .doOnNext { messages ->
                    if (messages.size == 1) {
                        smsManager.sendTextMessage(params.address, null, messages[0].first, messages[0].second, if (prefs.delivery.get()) messages[0].third else null)
                    } else {
                        val parts = ArrayList(messages.map { it.first })
                        val sentIntents = ArrayList(messages.map { it.second })
                        val deliveredIntents = ArrayList(messages.map { it.third })

                        smsManager.sendMultipartTextMessage(params.address, null, parts, sentIntents, if (prefs.delivery.get()) deliveredIntents else null)
                    }
                }
                .map { Unit }
    }

    private fun createContentValues(threadId: Long, address: String, body: String) = ContentValues().apply {
        put(Telephony.Sms.ADDRESS, address)
        put(Telephony.Sms.BODY, body)
        put(Telephony.Sms.DATE, System.currentTimeMillis())
        put(Telephony.Sms.READ, true)
        put(Telephony.Sms.SEEN, true)
        put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
        put(Telephony.Sms.THREAD_ID, threadId)
    }

}