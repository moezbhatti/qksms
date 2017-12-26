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
package com.moez.QKSMS.presentation.common.widget

import android.content.Context
import android.content.res.ColorStateList
import android.support.v7.widget.SwitchCompat
import android.util.AttributeSet
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.withAlpha
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class QkSwitch @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SwitchCompat(context, attrs) {

    @Inject lateinit var colors: Colors

    private val disposables = CompositeDisposable()

    init {
        appComponent.inject(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val states = arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_checked),
                intArrayOf())

        disposables += Observables.combineLatest(colors.theme, colors.switchThumbEnabled, colors.switchThumbDisabled,
                { color, enabled, disabled -> intArrayOf(disabled, color, enabled) })
                .map { values -> ColorStateList(states, values) }
                .subscribe { tintList -> thumbTintList = tintList }

        disposables += Observables.combineLatest(colors.theme, colors.switchTrackEnabled, colors.switchTrackDisabled,
                { color, enabled, disabled -> intArrayOf(disabled, color.withAlpha(0x4D), enabled) })
                .map { values -> ColorStateList(states, values) }
                .subscribe { tintList -> trackTintList = tintList }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposables.clear()
    }

}