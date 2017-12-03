package com.moez.QKSMS.presentation.settings

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.domain.interactor.FullSync
import com.moez.QKSMS.presentation.base.QkViewModel
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel : QkViewModel<SettingsView, SettingsState>(SettingsState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var prefs: RxSharedPreferences
    @Inject lateinit var fullSync: FullSync

    init {
        AppComponentManager.appComponent.inject(this)

        disposables += fullSync

        disposables += prefs.getBoolean("defaultSms")
                .asObservable()
                .subscribe { isDefaultSmsApp ->
                    newState { it.copy(isDefaultSmsApp = isDefaultSmsApp) }
                }
    }

    override fun bindView(view: SettingsView) {
        super.bindView(view)

        // Force update the view once we receive notification that the fragment's preference view has been created
        intents += view.preferencesAddedIntent.subscribe {
            newState { it.copy() }
        }

        intents += view.preferenceClickIntent.subscribe {
            Timber.v("Preference click: ${it.key}")

            when (it.key) {
                "defaultSms" -> {
                    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
                        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                    }
                    context.startActivity(intent)
                }

                "theme" -> {
                    newState { it.copy(selectingTheme = true) }
                }

                "sync" -> {
                    newState { it.copy(syncing = true) }
                    fullSync.execute(Unit, {
                        newState { it.copy(syncing = false) }
                    })
                }
            }
        }

        intents += view.themeSelectedIntent.subscribe { color ->
            prefs.getInteger("theme").set(color)
            newState { it.copy(selectingTheme = false) }
        }
    }
}