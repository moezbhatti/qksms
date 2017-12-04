package com.moez.QKSMS.common.util

import android.content.Context
import com.moez.QKSMS.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Colors @Inject constructor(context: Context, prefs: Preferences) {

    val theme: Observable<Int>

    val switchThumbEnabled: Observable<Int>
    val switchThumbDisabled: Observable<Int>
    val switchTrackEnabled: Observable<Int>
    val switchTrackDisabled: Observable<Int>

    val bubbleColor = 0xFFFFFFFF.toInt()

    val textPrimary = 0xFF49555F.toInt()
    val textSecondary = 0xFF70808D.toInt()
    val textTertiary = 0xFFB7B9C0.toInt()

    init {
        val colorSubject: Subject<Int> = BehaviorSubject.createDefault(0xFF008389.toInt())

        prefs.theme.asObservable().subscribe {
            colorSubject.onNext(it)
        }

        theme = colorSubject
                .buffer(80, TimeUnit.MILLISECONDS)
                .filter { it.isNotEmpty() }
                .map { it.last() }
                .observeOn(AndroidSchedulers.mainThread())

        switchThumbEnabled = prefs.dark.asObservable()
                .map { dark ->
                    when (dark) {
                        true -> R.color.switch_thumb_enabled_dark
                        false -> R.color.switch_thumb_enabled_light
                    }
                }
                .map { res -> context.resources.getColor(res) }

        switchThumbDisabled = prefs.dark.asObservable()
                .map { dark ->
                    when (dark) {
                        true -> R.color.switch_thumb_disabled_dark
                        false -> R.color.switch_thumb_disabled_light
                    }
                }
                .map { res -> context.resources.getColor(res) }

        switchTrackEnabled = prefs.dark.asObservable()
                .map { dark ->
                    when (dark) {
                        true -> R.color.switch_track_enabled_dark
                        false -> R.color.switch_track_enabled_light
                    }
                }
                .map { res -> context.resources.getColor(res) }

        switchTrackDisabled = prefs.dark.asObservable()
                .map { dark ->
                    when (dark) {
                        true -> R.color.switch_track_disabled_dark
                        false -> R.color.switch_track_disabled_light
                    }
                }
                .map { res -> context.resources.getColor(res) }
    }

}