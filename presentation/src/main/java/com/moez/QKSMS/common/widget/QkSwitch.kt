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
package com.moez.QKSMS.common.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.getColorCompat
import com.moez.QKSMS.common.util.extensions.withAlpha
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.util.Preferences
import javax.inject.Inject

class QkSwitch @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SwitchCompat(context, attrs) {

    @Inject lateinit var colors: Colors
    @Inject lateinit var prefs: Preferences

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

            val themeColor = colors.theme().theme

            val switchThumbEnabled: Int = prefs.night.get()
                    .let { night -> if (night) R.color.switchThumbEnabledDark else R.color.switchThumbEnabledLight }
                    .let { res -> context.getColorCompat(res) }

            val switchThumbDisabled: Int = prefs.night.get()
                    .let { night -> if (night) R.color.switchThumbDisabledDark else R.color.switchThumbDisabledLight }
                    .let { res -> context.getColorCompat(res) }

            val switchTrackEnabled: Int = prefs.night.get()
                    .let { night -> if (night) R.color.switchTrackEnabledDark else R.color.switchTrackEnabledLight }
                    .let { res -> context.getColorCompat(res) }

            val switchTrackDisabled: Int = prefs.night.get()
                    .let { night -> if (night) R.color.switchTrackDisabledDark else R.color.switchTrackDisabledLight }
                    .let { res -> context.getColorCompat(res) }

            thumbTintList = ColorStateList(states, intArrayOf(switchThumbDisabled, themeColor, switchThumbEnabled))
            trackTintList = ColorStateList(states, intArrayOf(switchTrackDisabled, themeColor.withAlpha(0x4D), switchTrackEnabled))
        }
    }
}