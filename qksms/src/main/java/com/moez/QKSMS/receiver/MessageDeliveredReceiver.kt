package com.moez.QKSMS.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import java.util.*

class MessageDeliveredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val uri = Uri.parse(intent.getStringExtra("uri"))

        when (resultCode) {
            Activity.RESULT_OK -> { // TODO notify about delivery
                val values = ContentValues()
                values.put("status", Telephony.TextBasedSmsColumns.STATUS_COMPLETE)
                values.put("date_sent", Calendar.getInstance().timeInMillis)
                values.put("read", true)
                context.contentResolver.update(uri, values, null, null)
            }

            Activity.RESULT_CANCELED -> { // TODO notify about delivery failure
                val values = ContentValues()
                values.put("status", Telephony.TextBasedSmsColumns.STATUS_FAILED)
                values.put("date_sent", Calendar.getInstance().timeInMillis)
                values.put("read", true)
                values.put("error_code", resultCode)
                context.contentResolver.update(uri, values, null, null)
            }
        }
    }

}