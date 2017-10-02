package com.moez.QKSMS.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.domain.interactor.MarkFailed
import com.moez.QKSMS.domain.interactor.MarkSent
import javax.inject.Inject

class MessageSentReceiver : BroadcastReceiver() {

    @Inject lateinit var markSent: MarkSent
    @Inject lateinit var markFailed: MarkFailed

    override fun onReceive(context: Context, intent: Intent) {
        AppComponentManager.appComponent.inject(this)

        val uri = Uri.parse(intent.getStringExtra("uri"))

        when (resultCode) {
            Activity.RESULT_OK -> markSent.execute({}, uri)

            SmsManager.RESULT_ERROR_GENERIC_FAILURE,
            SmsManager.RESULT_ERROR_NO_SERVICE,
            SmsManager.RESULT_ERROR_NULL_PDU,
            SmsManager.RESULT_ERROR_RADIO_OFF -> markFailed.execute({}, MarkFailed.Params(uri, resultCode))
        }
    }
}