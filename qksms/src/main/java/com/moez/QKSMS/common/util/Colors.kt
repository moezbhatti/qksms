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

    val background: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.backgroundDark else R.color.white }
            .map { res -> context.resources.getColor(res) }

    val composeBackground: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.backgroundDark else R.color.backgroundLight }
            .map { res -> context.resources.getColor(res) }

    val bubble: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.bubbleDark else R.color.bubbleLight }
            .map { res -> context.resources.getColor(res) }

    val switchThumbEnabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switch_thumb_enabled_dark else R.color.switch_thumb_enabled_light }
            .map { res -> context.resources.getColor(res) }

    val switchThumbDisabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switch_thumb_disabled_dark else R.color.switch_thumb_disabled_light }
            .map { res -> context.resources.getColor(res) }

    val switchTrackEnabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switch_track_enabled_dark else R.color.switch_track_enabled_light }
            .map { res -> context.resources.getColor(res) }

    val switchTrackDisabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switch_track_disabled_dark else R.color.switch_track_disabled_light }
            .map { res -> context.resources.getColor(res) }

    val textPrimary = 0xFF49555F.toInt()
    val textSecondary = 0xFF70808D.toInt()
    val textTertiary = 0xFFB7B9C0.toInt()

}