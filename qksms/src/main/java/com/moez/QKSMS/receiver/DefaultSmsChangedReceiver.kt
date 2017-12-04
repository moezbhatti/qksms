package com.moez.QKSMS.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.support.annotation.RequiresApi
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.Preferences
import com.moez.QKSMS.domain.interactor.PartialSync
import javax.inject.Inject

class DefaultSmsChangedReceiver : BroadcastReceiver() {

    @Inject lateinit var partialSync: PartialSync
    @Inject lateinit var prefs: Preferences

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context, intent: Intent) {
        AppComponentManager.appComponent.inject(this)

        val isDefaultSmsApp = intent.getBooleanExtra(Telephony.Sms.Intents.EXTRA_IS_DEFAULT_SMS_APP, false)
        prefs.defaultSms.set(isDefaultSmsApp)

        if (isDefaultSmsApp) {
            val pendingResult = goAsync()
            partialSync.execute(Unit, { pendingResult.finish() })
        }
    }

}