package com.moez.QKSMS.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.domain.interactor.MarkSeen
import javax.inject.Inject

class MarkSeenReceiver : BroadcastReceiver() {

    @Inject lateinit var markSeen: MarkSeen

    override fun onReceive(context: Context, intent: Intent) {
        AppComponentManager.appComponent.inject(this)

        val threadId = intent.getLongExtra("threadId", 0)
        markSeen.execute(threadId)
    }

}