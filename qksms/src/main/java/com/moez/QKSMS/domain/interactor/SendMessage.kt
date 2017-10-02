package com.moez.QKSMS.domain.interactor

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.receiver.MessageDeliveredReceiver
import com.moez.QKSMS.receiver.MessageSentReceiver
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SendMessage @Inject constructor(
        val context: Context,
        val messageRepo: MessageRepository) : Interactor<Unit, SendMessage.Params>() {

    data class Params(val threadId: Long, val address: String, val body: String)

    override fun buildUseCaseObservable(params: Params): Flowable<Unit> {
        val values = ContentValues()
        values.put(Telephony.Sms.ADDRESS, params.address)
        values.put(Telephony.Sms.BODY, params.body)
        values.put(Telephony.Sms.DATE, System.currentTimeMillis())
        values.put(Telephony.Sms.READ, true)
        values.put(Telephony.Sms.SEEN, true)
        values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
        values.put(Telephony.Sms.THREAD_ID, params.threadId)

        val contentResolver = context.contentResolver
        return Flowable.just(values)
                .subscribeOn(Schedulers.io())
                .map { contentResolver.insert(Telephony.Sms.CONTENT_URI, values) }
                .doOnNext { uri ->
                    messageRepo.addMessageFromUri(uri)

                    val sentIntent = Intent(context, MessageSentReceiver::class.java).putExtra("uri", uri.toString())
                    val sentPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), sentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val deliveredIntent = Intent(context, MessageDeliveredReceiver::class.java).putExtra("uri", uri.toString())
                    val deliveredPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(params.address, null, params.body, sentPI, deliveredPI)
                }
                .map { Unit }
    }

}