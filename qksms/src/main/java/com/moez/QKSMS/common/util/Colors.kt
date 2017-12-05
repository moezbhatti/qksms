package com.moez.QKSMS.common.util

import android.content.Context
import com.moez.QKSMS.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Colors @Inject constructor(context: Context, prefs: Preferences) {

    val theme: Observable<Int> = prefs.theme.asObservable()
            .buffer(80, TimeUnit.MILLISECONDS)
            .filter { it.isNotEmpty() }
            .map { it.last() }
            .observeOn(AndroidSchedulers.mainThread())

    val switchThumbEnabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark ->
                when (dark) {
                    true -> R.color.switch_thumb_enabled_dark
                    false -> R.color.switch_thumb_enabled_light
                }
            }
            .map { res -> context.resources.getColor(res) }

    val switchThumbDisabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark ->
                when (dark) {
                    true -> R.color.switch_thumb_disabled_dark
                    false -> R.color.switch_thumb_disabled_light
                }
            }
            .map { res -> context.resources.getColor(res) }

    val switchTrackEnabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark ->
                when (dark) {
                    true -> R.color.switch_track_enabled_dark
                    false -> R.color.switch_track_enabled_light
                }
            }
            .map { res -> context.resources.getColor(res) }

    val switchTrackDisabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark ->
                when (dark) {
                    true -> R.color.switch_track_disabled_dark
                    false -> R.color.switch_track_disabled_light
                }
            }
            .map { res -> context.resources.getColor(res) }

    val bubbleColor = 0xFFFFFFFF.toInt()

    val textPrimary = 0xFF49555F.toInt()
    val textSecondary = 0xFF70808D.toInt()
    val textTertiary = 0xFFB7B9C0.toInt()

}