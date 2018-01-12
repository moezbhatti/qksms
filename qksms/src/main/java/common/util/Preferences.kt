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
package common.util

import android.provider.Settings
import com.f2prateek.rx.preferences2.RxSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor(rxPrefs: RxSharedPreferences) {

    val defaultSms = rxPrefs.getBoolean("defaultSms", false)
    val theme = rxPrefs.getInteger("theme", 0xFF008389.toInt())
    val dark = rxPrefs.getBoolean("dark", false)
    val autoEmoji = rxPrefs.getBoolean("autoEmoji", true)
    val notifications = rxPrefs.getBoolean("notifications", true)
    val vibration = rxPrefs.getBoolean("vibration", true)
    val ringtone = rxPrefs.getString("ringtone", Settings.System.DEFAULT_NOTIFICATION_URI.toString())
    val delivery = rxPrefs.getBoolean("delivery", false)
    val unicode = rxPrefs.getBoolean("unicode", false)
    val mms = rxPrefs.getBoolean("mms", true)
    val mmsSize = rxPrefs.getLong("mmsSize", -1)

}