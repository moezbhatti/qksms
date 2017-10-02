package com.moez.QKSMS.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.repository.MessageRepository
import javax.inject.Inject

class MessageDeliveredReceiver : BroadcastReceiver() {

    @Inject lateinit var messageRepo: MessageRepository

    override fun onReceive(context: Context, intent: Intent) {
        val uri = Uri.parse(intent.getStringExtra("uri"))
        AppComponentManager.appComponent.inject(this)

        when (resultCode) {
        // TODO notify about delivery
            Activity.RESULT_OK -> messageRepo.markDelivered(uri)

        // TODO notify about delivery failure
            Activity.RESULT_CANCELED -> messageRepo.markDeliveryFailed(uri, resultCode)
        }
    }

}