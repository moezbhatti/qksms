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
package presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import common.di.appComponent
import common.util.DateFormatter
import common.util.Preferences
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NightModeReceiver : BroadcastReceiver() {

    @Inject lateinit var dateFormatter: DateFormatter
    @Inject lateinit var prefs: Preferences

    init {
        appComponent.inject(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        // If night mode is not on auto, then there's nothing to do here
        if (prefs.nightMode.get() != Preferences.NIGHT_MODE_AUTO) {
            return
        }

        val nightStartTime = getPreviousInstanceOfTime(prefs.nightStart.get())
        val nightEndTime = getPreviousInstanceOfTime(prefs.nightEnd.get())

        // If the last nightStart was more recent than the last nightEnd, then it's night time
        prefs.night.set(nightStartTime > nightEndTime)
    }

    /**
     * Returns a Calendar set to the most recent occurrence of this time
     */
    private fun getPreviousInstanceOfTime(time: String): Calendar {
        val calendar = dateFormatter.parseTime(time)
        val currentTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)

        while (calendar.timeInMillis > currentTime) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return calendar
    }
}