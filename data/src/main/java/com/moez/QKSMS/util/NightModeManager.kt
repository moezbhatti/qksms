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
package com.moez.QKSMS.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.moez.QKSMS.receiver.NightModeReceiver
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NightModeManager @Inject constructor(
        private val context: Context,
        private val prefs: Preferences) {

    fun updateCurrentTheme() {
        // If night mode is not on auto, then there's nothing to do here
        if (prefs.nightMode.get() != Preferences.NIGHT_MODE_AUTO) {
            return
        }

        val nightStartTime = getPreviousInstanceOfTime(prefs.nightStart.get())
        val nightEndTime = getPreviousInstanceOfTime(prefs.nightEnd.get())

        // If the last nightStart was more recent than the last nightEnd, then it's night time
        prefs.night.set(nightStartTime > nightEndTime)
    }

    fun updateNightMode(mode: Int) {
        prefs.nightMode.set(mode)

        // If it's not on auto mode, set the appropriate night mode
        if (mode != Preferences.NIGHT_MODE_AUTO) {
            prefs.night.set(mode == Preferences.NIGHT_MODE_ON)
        }

        updateAlarms()
    }

    fun setNightStart(hour: Int, minute: Int) {
        prefs.nightStart.set("$hour:$minute")
        updateAlarms()
    }

    fun setNightEnd(hour: Int, minute: Int) {
        prefs.nightEnd.set("$hour:$minute")
        updateAlarms()
    }

    private fun updateAlarms() {
        val dayCalendar = createCalendar(prefs.nightEnd.get())
        val day = Intent(context, NightModeReceiver::class.java)
        val dayIntent = PendingIntent.getBroadcast(context, 0, day, 0)

        val nightCalendar = createCalendar(prefs.nightStart.get())
        val night = Intent(context, NightModeReceiver::class.java)
        val nightIntent = PendingIntent.getBroadcast(context, 1, night, 0)

        context.sendBroadcast(day)
        context.sendBroadcast(night)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (prefs.nightMode.get() == Preferences.NIGHT_MODE_AUTO) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, dayCalendar.timeInMillis, AlarmManager.INTERVAL_DAY, dayIntent)
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, nightCalendar.timeInMillis, AlarmManager.INTERVAL_DAY, nightIntent)
        } else {
            alarmManager.cancel(dayIntent)
            alarmManager.cancel(nightIntent)
        }
    }

    private fun createCalendar(time: String): Calendar {
        val calendar = parseTime(time)

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        }
    }

    /**
     * Parses the hour and minute out of the [time], which should be formatted h:mm
     */
    fun parseTime(time: String): Calendar {
        return tryOrNull {
            val parsedTime = SimpleDateFormat("H:mm", Locale.US).parse(time)
            Calendar.getInstance().apply { this.time = parsedTime }
        } ?: tryOrNull {
            // Parse the legacy timestamp format (<=3.1.3)
            val parsedTime = SimpleDateFormat("h:mm a", Locale.US).parse(time)
            Calendar.getInstance().apply { this.time = parsedTime }
        } ?: Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 18) }
    }

    /**
     * Returns a Calendar set to the most recent occurrence of this time
     */
    private fun getPreviousInstanceOfTime(time: String): Calendar {
        val currentTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)
        val calendar = createCalendar(time)

        while (calendar.timeInMillis > currentTime) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return calendar
    }

}