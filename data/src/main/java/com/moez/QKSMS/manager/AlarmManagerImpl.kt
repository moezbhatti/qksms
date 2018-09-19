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
package com.moez.QKSMS.manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.moez.QKSMS.receiver.SendScheduledMessageReceiver
import javax.inject.Inject

class AlarmManagerImpl @Inject constructor(private val context: Context) : AlarmManager {

    override fun getScheduledMessageIntent(id: Long): PendingIntent {
        val intent = Intent(context, SendScheduledMessageReceiver::class.java).putExtra("id", id)
        return PendingIntent.getBroadcast(context, id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun setAlarm(date: Long, intent: PendingIntent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, date, intent)
        } else {
            alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, date, intent)
        }
    }

}