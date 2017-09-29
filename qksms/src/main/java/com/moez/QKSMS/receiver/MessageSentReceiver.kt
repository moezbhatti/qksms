package com.moez.QKSMS.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.repository.MessageRepository
import javax.inject.Inject

class MessageSentReceiver : BroadcastReceiver() {

    @Inject lateinit var messageRepo: MessageRepository

    override fun onReceive(context: Context, intent: Intent) {
        AppComponentManager.appComponent.inject(this)

        val uri = Uri.parse(intent.getStringExtra("uri"))

        when (resultCode) {
            Activity.RESULT_OK -> messageRepo.markSent(uri)

            SmsManager.RESULT_ERROR_GENERIC_FAILURE,
            SmsManager.RESULT_ERROR_NO_SERVICE,
            SmsManager.RESULT_ERROR_NULL_PDU,
            SmsManager.RESULT_ERROR_RADIO_OFF -> messageRepo.markFailed(uri, resultCode)
        }
    }
}