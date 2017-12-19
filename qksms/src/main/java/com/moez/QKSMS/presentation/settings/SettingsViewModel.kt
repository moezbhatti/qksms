package com.moez.QKSMS.presentation.settings

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.Preferences
import com.moez.QKSMS.domain.interactor.FullSync
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.common.base.QkViewModel
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel : QkViewModel<SettingsView, SettingsState>(SettingsState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var fullSync: FullSync

    init {
        appComponent.inject(this)

        disposables += prefs.defaultSms
                .asObservable()
                .subscribe { isDefaultSmsApp ->
                    newState { it.copy(isDefaultSmsApp = isDefaultSmsApp) }
                }

        disposables += prefs.dark.asObservable().subscribe { darkModeEnabled ->
            newState { it.copy(darkModeEnabled = darkModeEnabled) }
        }

        disposables += prefs.autoEmoji.asObservable().subscribe { autoEmojiEnabled ->
            newState { it.copy(autoEmojiEnabled = autoEmojiEnabled) }
        }

        disposables += prefs.notifications.asObservable().subscribe { notificationsEnabled ->
            newState { it.copy(notificationsEnabled = notificationsEnabled) }
        }

        disposables += prefs.vibration.asObservable().subscribe { vibrationEnabled ->
            newState { it.copy(vibrationEnabled = vibrationEnabled) }
        }

        disposables += prefs.ringtone.asObservable().subscribe { ringtone ->
        }

        disposables += prefs.delivery.asObservable().subscribe { deliveryEnabled ->
            newState { it.copy(deliveryEnabled = deliveryEnabled) }
        }

        disposables += prefs.split.asObservable().subscribe { splitSmsEnabled ->
            newState { it.copy(splitSmsEnabled = splitSmsEnabled) }
        }

        disposables += prefs.unicode.asObservable().subscribe { stripUnicodeEnabled ->
            newState { it.copy(stripUnicodeEnabled = stripUnicodeEnabled) }
        }

        disposables += prefs.mms.asObservable().subscribe { mmsEnabled ->
            newState { it.copy(mmsEnabled = mmsEnabled) }
        }

        disposables += prefs.mmsSize.asObservable().subscribe { maxMmsSize ->
            newState { it.copy(maxMmsSize = maxMmsSize.toString()) }
        }

        disposables += fullSync
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

                R.id.theme -> newState { it.copy(selectingTheme = true) }

                R.id.dark -> prefs.dark.set(!prefs.dark.get())

                R.id.autoEmoji -> prefs.autoEmoji.set(!prefs.autoEmoji.get())

                R.id.notificationsO -> navigator.showNotificationSettings()

                R.id.notifications -> prefs.notifications.set(!prefs.notifications.get())

                R.id.vibration -> prefs.vibration.set(!prefs.vibration.get())

                R.id.ringtone -> {
                }

                R.id.delivery -> prefs.delivery.set(!prefs.delivery.get())

                R.id.split -> prefs.split.set(!prefs.split.get())

                R.id.unicode -> prefs.unicode.set(!prefs.unicode.get())

                R.id.mms -> prefs.mms.set(!prefs.mms.get())

                R.id.mmsSize -> {
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