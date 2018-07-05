package com.moez.QKSMS.feature.settings

import android.content.Context
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.base.QkPresenter
import com.moez.QKSMS.common.util.BillingManager
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.interactor.SyncMessages
import com.moez.QKSMS.util.NightModeManager
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.rxkotlin.withLatestFrom
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SettingsPresenter @Inject constructor(
        private val context: Context,
        private val billingManager: BillingManager,
        private val colors: Colors,
        private val dateFormatter: DateFormatter,
        private val navigator: Navigator,
        private val nightModeManager: NightModeManager,
        private val prefs: Preferences,
        private val syncMessages: SyncMessages
) : QkPresenter<SettingsView, SettingsState>(SettingsState(theme = colors.theme().theme)) {

    override fun onCreate(view: SettingsView) {
        super.onCreate(view)

        val nightModeLabels = context.resources.getStringArray(R.array.night_modes)
        prefs.nightMode.asObservable()
                .autoDisposable(view.scope())
                .subscribe { nightMode ->
                    newState { copy(nightModeSummary = nightModeLabels[nightMode], nightModeId = nightMode) }
                }

        prefs.nightStart.asObservable()
                .map { time -> nightModeManager.parseTime(time) }
                .map { calendar -> calendar.timeInMillis }
                .map { millis -> dateFormatter.getTimestamp(millis) }
                .autoDisposable(view.scope())
                .subscribe { nightStart -> newState { copy(nightStart = nightStart) } }

        prefs.nightEnd.asObservable()
                .map { time -> nightModeManager.parseTime(time) }
                .map { calendar -> calendar.timeInMillis }
                .map { millis -> dateFormatter.getTimestamp(millis) }
                .autoDisposable(view.scope())
                .subscribe { nightEnd -> newState { copy(nightEnd = nightEnd) } }

        prefs.black.asObservable()
                .autoDisposable(view.scope())
                .subscribe { black -> newState { copy(black = black) } }

        prefs.notifications().asObservable()
                .autoDisposable(view.scope())
                .subscribe { enabled -> newState { copy(notificationsEnabled = enabled) } }

        prefs.autoEmoji.asObservable()
                .autoDisposable(view.scope())
                .subscribe { enabled -> newState { copy(autoEmojiEnabled = enabled) } }

        val delayedSendingLabels = context.resources.getStringArray(R.array.delayed_sending_labels)
        prefs.sendDelay.asObservable()
                .autoDisposable(view.scope())
                .subscribe { id -> newState { copy(sendDelaySummary = delayedSendingLabels[id], sendDelayId = id) } }

        prefs.delivery.asObservable()
                .autoDisposable(view.scope())
                .subscribe { enabled -> newState { copy(deliveryEnabled = enabled) } }

        val textSizeLabels = context.resources.getStringArray(R.array.text_sizes)
        prefs.textSize.asObservable()
                .autoDisposable(view.scope())
                .subscribe { textSize ->
                    newState { copy(textSizeSummary = textSizeLabels[textSize], textSizeId = textSize) }
                }

        prefs.systemFont.asObservable()
                .autoDisposable(view.scope())
                .subscribe { enabled -> newState { copy(systemFontEnabled = enabled) } }

        prefs.unicode.asObservable()
                .autoDisposable(view.scope())
                .subscribe { enabled -> newState { copy(stripUnicodeEnabled = enabled) } }

        val mmsSizeLabels = context.resources.getStringArray(R.array.mms_sizes)
        val mmsSizeIds = context.resources.getIntArray(R.array.mms_sizes_ids)
        prefs.mmsSize.asObservable()
                .autoDisposable(view.scope())
                .subscribe { maxMmsSize ->
                    val index = mmsSizeIds.indexOf(maxMmsSize)
                    newState { copy(maxMmsSizeSummary = mmsSizeLabels[index], maxMmsSizeId = maxMmsSize) }
                }

        view.preferenceClickIntent
                .autoDisposable(view.scope())
                .subscribe {
                    Timber.v("Preference click: ${context.resources.getResourceName(it.id)}")

                    when (it.id) {
                        R.id.theme -> navigator.showThemePicker()

                        R.id.night -> view.showNightModeDialog()

                        R.id.nightStart -> {
                            val date = nightModeManager.parseTime(prefs.nightStart.get())
                            view.showStartTimePicker(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE))
                        }

                        R.id.nightEnd -> {
                            val date = nightModeManager.parseTime(prefs.nightEnd.get())
                            view.showEndTimePicker(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE))
                        }

                        R.id.black -> prefs.black.set(!prefs.black.get())

                        R.id.autoEmoji -> prefs.autoEmoji.set(!prefs.autoEmoji.get())

                        R.id.notifications -> navigator.showNotificationSettings()

                        R.id.delayed -> view.showDelayDurationDialog()

                        R.id.delivery -> prefs.delivery.set(!prefs.delivery.get())

                        R.id.textSize -> view.showTextSizePicker()

                        R.id.systemFont -> prefs.systemFont.set(!prefs.systemFont.get())

                        R.id.unicode -> prefs.unicode.set(!prefs.unicode.get())

                        R.id.mmsSize -> view.showMmsSizePicker()

                        R.id.sync -> {
                            newState { copy(syncing = true) }
                            syncMessages.execute(Unit) {
                                newState { copy(syncing = false) }
                            }
                        }

                        R.id.about -> view.showAbout()
                    }
                }

        view.nightModeSelectedIntent
                .withLatestFrom(billingManager.upgradeStatus) { mode, upgraded ->
                    if (!upgraded && mode == Preferences.NIGHT_MODE_AUTO) {
                        view.showQksmsPlusSnackbar()
                    } else {
                        nightModeManager.updateNightMode(mode)
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.viewQksmsPlusIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showQksmsPlusActivity("settings_night") }

        view.startTimeSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { nightModeManager.setNightStart(it.first, it.second) }

        view.endTimeSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { nightModeManager.setNightEnd(it.first, it.second) }

        view.textSizeSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { prefs.textSize.set(it) }

        view.sendDelayChangedIntent
                .withLatestFrom(billingManager.upgradeStatus) { duration, upgraded ->
                    if (!upgraded && duration != 0) {
                        view.showQksmsPlusSnackbar()
                    } else {
                        prefs.sendDelay.set(duration)
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.mmsSizeSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { prefs.mmsSize.set(it) }
    }

}