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
package presentation.feature.settings

import android.net.Uri
import io.reactivex.Observable
import io.reactivex.subjects.Subject
import presentation.common.base.QkView
import presentation.common.widget.PreferenceView

interface SettingsView : QkView<SettingsState> {

    val preferenceClickIntent: Subject<PreferenceView>
    val nightModeSelectedIntent: Observable<Int>
    val startTimeSelectedIntent: Subject<Pair<Int, Int>>
    val endTimeSelectedIntent: Subject<Pair<Int, Int>>
    val ringtoneSelectedIntent: Observable<String>
    val mmsSizeSelectedIntent: Observable<Int>

    fun showNightModeDialog()
    fun dismissNightModeDialog()
    fun showStartTimePicker(hour: Int, minute: Int)
    fun showEndTimePicker(hour: Int, minute: Int)
    fun showRingtonePicker(default: Uri)
    fun showMmsSizePicker()
    fun dismissMmsSizePicker()
}
