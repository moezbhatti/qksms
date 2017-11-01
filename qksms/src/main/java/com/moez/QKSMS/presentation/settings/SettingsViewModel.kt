package com.moez.QKSMS.presentation.settings

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.domain.interactor.SyncConversations
import com.moez.QKSMS.presentation.base.QkViewModel
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel : QkViewModel<SettingsView, SettingsState>(SettingsState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var syncConversations: SyncConversations

    init {
        AppComponentManager.appComponent.inject(this)
    }

    override fun bindIntents(view: SettingsView) {
        super.bindIntents(view)

        view.preferenceClickIntent.subscribe {
            Timber.v("Preference click: ${it.key}")

            when (it.key) {
                "defaultSms" -> {
                    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
                        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                    }
                    context.startActivity(intent)
                }

                "sync" -> {
                    newState { it.copy(syncing = true) }
                    syncConversations.execute(Unit, {
                        newState { it.copy(syncing = false) }
                    })
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncConversations.dispose()
    }
}