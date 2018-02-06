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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import presentation.receiver.NightModeReceiver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NightModeManager @Inject constructor(
        private val context: Context,
        private val dateFormatter: DateFormatter,
        private val prefs: Preferences) {

    fun updateNightMode(mode: Int) {
        prefs.nightMode.set(mode)

        // If it's not on auto mode, set the appropriate night mode
        if (mode != Preferences.NIGHT_MODE_AUTO) {
            prefs.night.set(mode == Preferences.NIGHT_MODE_ON)
        }

        updateAlarms()
    }

    fun updateAlarms() {
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
        val calendar = dateFormatter.parseTime(time)

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        }
    }

}