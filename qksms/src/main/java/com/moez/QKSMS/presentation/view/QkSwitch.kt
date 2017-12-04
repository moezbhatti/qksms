package com.moez.QKSMS.presentation.view

import android.content.Context
import android.content.res.ColorStateList
import android.support.v7.widget.SwitchCompat
import android.util.AttributeSet
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.common.util.extensions.withAlpha
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class QkSwitch @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SwitchCompat(context, attrs) {

    @Inject lateinit var themeManager: ThemeManager

    private val disposables = CompositeDisposable()

    init {
        AppComponentManager.appComponent.inject(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val states = arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_checked),
                intArrayOf())

        disposables += Observables.combineLatest(themeManager.color, themeManager.switchThumbEnabled, themeManager.switchThumbDisabled,
                { color, enabled, disabled -> intArrayOf(disabled, color, enabled) })
                .map { values -> ColorStateList(states, values) }
                .subscribe { tintList -> thumbTintList = tintList }

        disposables += Observables.combineLatest(themeManager.color, themeManager.switchTrackEnabled, themeManager.switchTrackDisabled,
                { color, enabled, disabled -> intArrayOf(disabled, color.withAlpha(0x4D), enabled) })
                .map { values -> ColorStateList(states, values) }
                .subscribe { tintList -> trackTintList = tintList }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposables.clear()
    }

}