package com.moez.QKSMS.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager

class MessageSentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val uri = Uri.parse(intent.getStringExtra("uri"))

        when (resultCode) {
            Activity.RESULT_OK -> {
                val values = ContentValues()
                values.put("type", Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT)
                values.put("read", true)
                context.contentResolver.update(uri, values, null, null)
            }

            SmsManager.RESULT_ERROR_GENERIC_FAILURE,
            SmsManager.RESULT_ERROR_NO_SERVICE,
            SmsManager.RESULT_ERROR_NULL_PDU,
            SmsManager.RESULT_ERROR_RADIO_OFF -> {
                val values = ContentValues()
                values.put("type", Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED)
                values.put("read", true)
                values.put("error_code", resultCode)
                context.contentResolver.update(uri, values, null, null)
            }
        }
    }
}