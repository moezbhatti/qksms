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
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor(private val rxPrefs: RxSharedPreferences) {

    companion object {
        const val NIGHT_MODE_OFF = 0
        const val NIGHT_MODE_ON = 1
        const val NIGHT_MODE_AUTO = 2
    }

    val defaultSms = rxPrefs.getBoolean("defaultSms", false)
    val theme = rxPrefs.getInteger("theme", 0xFF008389.toInt())
    val night = rxPrefs.getBoolean("night", false)
    val nightMode = rxPrefs.getInteger("nightModeSummary", NIGHT_MODE_OFF)
    val nightStart = rxPrefs.getString("nightStart", "6:00 PM")
    val nightEnd = rxPrefs.getString("nightEnd", "6:00 AM")
    val autoEmoji = rxPrefs.getBoolean("autoEmoji", true)
    val delivery = rxPrefs.getBoolean("delivery", false)
    val unicode = rxPrefs.getBoolean("unicode", false)
    val mms = rxPrefs.getBoolean("mms", true)
    val mmsSize = rxPrefs.getInteger("mmsSize", 100)

    fun notifications(threadId: Long = 0): Preference<Boolean> {
        val default = rxPrefs.getBoolean("notifications", true)

        return when (threadId) {
            0L -> default
            else -> rxPrefs.getBoolean("notifications_$threadId", default.get())
        }
    }

    fun vibration(threadId: Long = 0): Preference<Boolean> {
        val default = rxPrefs.getBoolean("vibration", true)

        return when (threadId) {
            0L -> default
            else -> rxPrefs.getBoolean("vibration$threadId", default.get())
        }
    }

    fun ringtone(threadId: Long = 0): Preference<String> {
        val default = rxPrefs.getString("ringtone", Settings.System.DEFAULT_NOTIFICATION_URI.toString())

        return when (threadId) {
            0L -> default
            else -> rxPrefs.getString("ringtone_$threadId", default.get())
        }
    }
}