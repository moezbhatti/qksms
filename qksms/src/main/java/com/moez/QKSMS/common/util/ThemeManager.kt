package com.moez.QKSMS.common.util

import android.animation.ValueAnimator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor() {

    val color: Observable<Int>

    val bubbleColor = 0xFFFFFFFF.toInt()

    val textPrimary = 0xFF49555F.toInt()
    val textSecondary = 0xFF70808D.toInt()
    val textTertiary = 0xFFB7B9C0.toInt()

    init {
        val colorSubject: Subject<Int> = BehaviorSubject.createDefault(0xFF008389.toInt())

        color = colorSubject
                .buffer(80, TimeUnit.MILLISECONDS)
                .filter { it.isNotEmpty() }
                .map { it.last() }
                .observeOn(AndroidSchedulers.mainThread())

        ValueAnimator.ofArgb(0xFF444444.toInt(), 0xFF008389.toInt()).run {
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            duration = 1500
            addUpdateListener { animation -> colorSubject.onNext(animation.animatedValue as Int) }
            start()
        }
    }

}