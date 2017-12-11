package com.moez.QKSMS.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.google.android.mms.ContentType
import com.google.android.mms.pdu.GenericPdu
import com.google.android.mms.pdu.NotificationInd
import com.google.android.mms.pdu.PduHeaders
import com.google.android.mms.pdu.PduParser
import timber.log.Timber

class MmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION == intent.action && ContentType.MMS_MESSAGE == intent.type) {
            val data = intent.getByteArrayExtra("data")
            val parser = PduParser(data)
            var pdu: GenericPdu? = null

            try {
                pdu = parser.parse()
            } catch (e: RuntimeException) {
                Timber.e("Invalid MMS WAP push", e)
            }

            if (pdu == null) {
                Timber.e("Invalid WAP push data")
                return
            }

            when (pdu.messageType) {
                PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND -> {
                    val notificationInd = pdu as NotificationInd
                    val location = String(notificationInd.contentLocation)
                    Timber.v("Received MMS notification: " + location)
                }

                PduHeaders.MESSAGE_TYPE_DELIVERY_IND -> {
                    Timber.v("Received delivery report")
                }

                PduHeaders.MESSAGE_TYPE_READ_ORIG_IND -> {
                    Timber.v("Received read report")
                }
            }
        }
    }

}