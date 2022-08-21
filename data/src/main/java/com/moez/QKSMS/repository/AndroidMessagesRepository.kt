package com.moez.QKSMS.repository

import android.content.Context
import android.provider.Telephony
import android.telephony.SmsMessage
import com.google.android.mms.util_alt.SqliteWrapper
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidMessagesRepositoryImpl @Inject constructor(
        private val context: Context)
    : AndroidMessagesRepository {

    override fun deleteMessage(messages: Array<SmsMessage>) {
        val senderNumbers = messages
                .map { it.displayOriginatingAddress }
                .distinct()
                .toTypedArray()

        Single.just(1)
                .delay(3, TimeUnit.SECONDS)
                .subscribe { x ->
                    senderNumbers.forEach {
                        val delete = SqliteWrapper.delete(context, context.contentResolver, Telephony.Sms.CONTENT_URI, "${Telephony.Sms.ADDRESS} = ?", arrayOf(it))
                        println("yolo $delete")
                    }
                }
    }
}