package com.moez.QKSMS.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.domain.interactor.MarkRead
import javax.inject.Inject

class MarkReadReceiver : BroadcastReceiver() {

    @Inject lateinit var markRead: MarkRead

    override fun onReceive(context: Context, intent: Intent) {
        AppComponentManager.appComponent.inject(this)

        val threadId = intent.getLongExtra("threadId", 0)
        markRead.execute({}, threadId)
    }

}