package com.moez.QKSMS.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.repository.MessageRepository
import javax.inject.Inject

class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var messageRepo: MessageRepository

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SmsReceiver", "Received SMS: $intent")
        AppComponentManager.appComponent.inject(this)

        intent.extras?.let { extras ->
            val pdus = extras.get("pdus") as Array<Any>
            val format = extras.getString("format")
            val messages = pdus.map { pdu ->
                if (Build.VERSION.SDK_INT > 23) {
                    SmsMessage.createFromPdu(pdu as ByteArray, format)
                } else {
                    SmsMessage.createFromPdu(pdu as ByteArray)
                }
            }

            messages[0]?.let { sms ->
                val address = sms.displayOriginatingAddress
                val time = sms.timestampMillis
                val body: String = messages
                        .map { message -> message.displayMessageBody }
                        .reduce { body, new -> body + new }

                messageRepo.insertMessage(address, body, time)
            }
        }
    }

}