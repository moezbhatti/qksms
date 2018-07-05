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
package com.moez.QKSMS.feature.settings

import com.moez.QKSMS.common.base.QkConductorView
import com.moez.QKSMS.common.widget.PreferenceView
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface SettingsView : QkConductorView<SettingsState> {

    val preferenceClickIntent: Subject<PreferenceView>
    val viewQksmsPlusIntent: Subject<Unit>
    val nightModeSelectedIntent: Observable<Int>
    val startTimeSelectedIntent: Subject<Pair<Int, Int>>
    val endTimeSelectedIntent: Subject<Pair<Int, Int>>
    val textSizeSelectedIntent: Subject<Int>
    val sendDelayChangedIntent: Observable<Int>
    val mmsSizeSelectedIntent: Observable<Int>

    fun showQksmsPlusSnackbar()
    fun showNightModeDialog()
    fun showStartTimePicker(hour: Int, minute: Int)
    fun showEndTimePicker(hour: Int, minute: Int)
    fun showTextSizePicker()
    fun showDelayDurationDialog()
    fun showMmsSizePicker()
    fun showAbout()
}
