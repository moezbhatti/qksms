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
package presentation.common.widget

import android.content.Context
import android.content.res.ColorStateList
import android.support.v7.widget.SwitchCompat
import android.util.AttributeSet
import com.uber.autodispose.android.scope
import com.uber.autodispose.kotlin.autoDisposable
import injection.appComponent
import common.util.Colors
import common.util.extensions.withAlpha
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class QkSwitch @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SwitchCompat(context, attrs) {

    @Inject lateinit var colors: Colors

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            val states = arrayOf(
                    intArrayOf(-android.R.attr.state_enabled),
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf())

            Observables.combineLatest(colors.theme, colors.switchThumbEnabled, colors.switchThumbDisabled,
                    { color, enabled, disabled -> intArrayOf(disabled, color, enabled) })
                    .map { values -> ColorStateList(states, values) }
                    .autoDisposable(scope())
                    .subscribe { tintList -> thumbTintList = tintList }

            Observables.combineLatest(colors.theme, colors.switchTrackEnabled, colors.switchTrackDisabled,
                    { color, enabled, disabled -> intArrayOf(disabled, color.withAlpha(0x4D), enabled) })
                    .map { values -> ColorStateList(states, values) }
                    .autoDisposable(scope())
                    .subscribe { tintList -> trackTintList = tintList }
        }
    }
}