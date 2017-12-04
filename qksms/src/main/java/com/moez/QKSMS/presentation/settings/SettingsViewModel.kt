package com.moez.QKSMS.presentation.settings

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.Preferences
import com.moez.QKSMS.domain.interactor.FullSync
import com.moez.QKSMS.presentation.base.QkViewModel
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel : QkViewModel<SettingsView, SettingsState>(SettingsState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var fullSync: FullSync

    init {
        AppComponentManager.appComponent.inject(this)

        disposables += fullSync

        disposables += prefs.defaultSms
                .asObservable()
                .subscribe { isDefaultSmsApp ->
                    newState { it.copy(isDefaultSmsApp = isDefaultSmsApp) }
                }
    }

    override fun bindView(view: SettingsView) {
        super.bindView(view)

        intents += view.preferenceClickIntent.subscribe {
            Timber.v("Preference click: ${context.resources.getResourceName(it.id)}")

            when (it.id) {
                R.id.defaultSms -> {
                    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
                        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                    }
                    context.startActivity(intent)
                }

                R.id.theme -> {
                    newState { it.copy(selectingTheme = true) }
                }

                R.id.sync -> {
                    newState { it.copy(syncing = true) }
                    fullSync.execute(Unit, {
                        newState { it.copy(syncing = false) }
                    })
                }
            }
        }

        intents += view.themeSelectedIntent.subscribe { color ->
            prefs.theme.set(color)
            newState { it.copy(selectingTheme = false) }
        }
    }
}