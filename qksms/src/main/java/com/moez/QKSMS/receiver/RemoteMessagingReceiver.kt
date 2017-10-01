package com.moez.QKSMS.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.RemoteInput
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.SendMessage
import javax.inject.Inject

class RemoteMessagingReceiver : BroadcastReceiver() {

    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var sendMessage: SendMessage

    override fun onReceive(context: Context, intent: Intent) {
        AppComponentManager.appComponent.inject(this)

        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val bundle = intent.extras
        if (remoteInput != null && bundle != null) {
            val address = bundle.getString("address")
            val threadId = bundle.getLong("threadId")
            val body = remoteInput.getCharSequence("body").toString()
            messageRepo.markRead(threadId)
            sendMessage.execute({}, SendMessage.Params(threadId, address, body))
        }
    }
}
