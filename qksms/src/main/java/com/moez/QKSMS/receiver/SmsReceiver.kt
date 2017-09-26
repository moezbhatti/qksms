package com.moez.QKSMS.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.repository.MessageRepository
import timber.log.Timber
import javax.inject.Inject

class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var messageRepo: MessageRepository

    override fun onReceive(context: Context, intent: Intent) {
        Timber.v("Received SMS: $intent")
        AppComponentManager.appComponent.inject(this)

        val messages = Sms.Intents.getMessagesFromIntent(intent)
        messages?.takeIf { it.isNotEmpty() }?.let { messages ->
            val address = messages[0].displayOriginatingAddress
            val time = messages[0].timestampMillis
            val body: String = messages
                    .map { message -> message.displayMessageBody }
                    .reduce { body, new -> body + new }

            messageRepo.insertReceivedMessage(address, body, time)
        }
    }

}