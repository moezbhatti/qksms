/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package presentation.feature.settings

import android.content.Context
import android.net.Uri
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.DateFormatter
import common.util.NightModeManager
import common.util.Preferences
import interactor.FullSync
import io.reactivex.rxkotlin.plusAssign
import presentation.common.Navigator
import presentation.common.base.QkViewModel
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SettingsViewModel : QkViewModel<SettingsView, SettingsState>(SettingsState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var dateFormatter: DateFormatter
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var nightModeManager: NightModeManager
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var fullSync: FullSync

    init {
        appComponent.inject(this)

        disposables += prefs.defaultSms
                .asObservable()
                .subscribe { isDefaultSmsApp ->
                    newState { it.copy(isDefaultSmsApp = isDefaultSmsApp) }
                }

        val nightModeLabels = context.resources.getStringArray(R.array.night_modes)
        disposables += prefs.nightMode.asObservable().subscribe { nightMode ->
            newState { it.copy(nightModeSummary = nightModeLabels[nightMode], nightModeId = nightMode) }
        }

        disposables += prefs.nightStart.asObservable().subscribe { nightStart ->
            newState { it.copy(nightStart = nightStart) }
        }

        disposables += prefs.nightEnd.asObservable().subscribe { nightEnd ->
            newState { it.copy(nightEnd = nightEnd) }
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

        disposables += prefs.delivery.asObservable().subscribe { deliveryEnabled ->
            newState { it.copy(deliveryEnabled = deliveryEnabled) }
        }

        disposables += prefs.unicode.asObservable().subscribe { stripUnicodeEnabled ->
            newState { it.copy(stripUnicodeEnabled = stripUnicodeEnabled) }
        }

        disposables += prefs.mms.asObservable().subscribe { mmsEnabled ->
            newState { it.copy(mmsEnabled = mmsEnabled) }
        }

        val mmsSizeLabels = context.resources.getStringArray(R.array.mms_sizes)
        val mmsSizeIds = context.resources.getIntArray(R.array.mms_sizes_ids)
        disposables += prefs.mmsSize.asObservable().subscribe { maxMmsSize ->
            val index = mmsSizeIds.indexOf(maxMmsSize)
            newState { it.copy(maxMmsSizeSummary = mmsSizeLabels[index], maxMmsSizeId = maxMmsSize) }
        }

        disposables += fullSync
    }

    override fun bindView(view: SettingsView) {
        super.bindView(view)

        view.preferenceClickIntent
                .autoDisposable(view.scope())
                .subscribe {
                    Timber.v("Preference click: ${context.resources.getResourceName(it.id)}")

                    when (it.id) {
                        R.id.defaultSms -> navigator.showDefaultSmsDialog()

                        R.id.theme -> navigator.showThemePicker()

                        R.id.night -> view.showNightModeDialog()

                        R.id.nightStart -> {
                            val date = dateFormatter.parseTime(prefs.nightStart.get())
                            view.showStartTimePicker(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE))
                        }

                        R.id.nightEnd -> {
                            val date = dateFormatter.parseTime(prefs.nightEnd.get())
                            view.showEndTimePicker(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE))
                        }

                        R.id.autoEmoji -> prefs.autoEmoji.set(!prefs.autoEmoji.get())

                        R.id.notificationsO -> navigator.showNotificationSettings()

                        R.id.notifications -> prefs.notifications.set(!prefs.notifications.get())

                        R.id.vibration -> prefs.vibration.set(!prefs.vibration.get())

                        R.id.ringtone -> view.showRingtonePicker(Uri.parse(prefs.ringtone.get()))

                        R.id.delivery -> prefs.delivery.set(!prefs.delivery.get())

                        R.id.unicode -> prefs.unicode.set(!prefs.unicode.get())

                        R.id.mms -> prefs.mms.set(!prefs.mms.get())

                        R.id.mmsSize -> view.showMmsSizePicker()

                        R.id.sync -> {
                            newState { it.copy(syncing = true) }
                            fullSync.execute(Unit, {
                                newState { it.copy(syncing = false) }
                            })
                        }
                    }
                }

        view.nightModeSelectedIntent
                .doOnNext { view.dismissNightModeDialog() }
                .autoDisposable(view.scope())
                .subscribe { nightMode -> nightModeManager.updateNightMode(nightMode) }

        view.startTimeSelectedIntent
                .doOnNext { nightModeManager.updateAlarms() }
                .map { dateFormatter.formatTime(it.first, it.second) }
                .autoDisposable(view.scope())
                .subscribe { prefs.nightStart.set(it) }

        view.endTimeSelectedIntent
                .doOnNext { nightModeManager.updateAlarms() }
                .map { dateFormatter.formatTime(it.first, it.second) }
                .autoDisposable(view.scope())
                .subscribe { prefs.nightEnd.set(it) }

        view.ringtoneSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { ringtone -> prefs.ringtone.set(ringtone) }

        view.mmsSizeSelectedIntent
                .doOnNext { view.dismissMmsSizePicker() }
                .autoDisposable(view.scope())
                .subscribe { prefs.mmsSize.set(it) }
    }
}