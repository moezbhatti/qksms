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
package com.moez.QKSMS.feature.notificationprefs

import android.net.Uri
import com.moez.QKSMS.common.base.QkView
import com.moez.QKSMS.common.widget.PreferenceView
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface NotificationPrefsView : QkView<NotificationPrefsState> {

    val preferenceClickIntent: Subject<PreferenceView>
    val previewModeSelectedIntent: Subject<Int>
    val ringtoneSelectedIntent: Observable<String>
    val actionsSelectedIntent: Subject<Int>

    fun showPreviewModeDialog()
    fun showRingtonePicker(default: Uri?)
    fun showActionDialog(selected: Int)
}
