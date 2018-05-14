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

import android.content.Context
import android.text.format.DateFormat
import common.util.extensions.isSameDay
import common.util.extensions.isSameWeek
import common.util.extensions.isSameYear
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateFormatter @Inject constructor(val context: Context) {

    /**
     * Replace 12 hour format with 24 hour format if necessary
     */
    private fun getFormatter(pattern: String): SimpleDateFormat {
        val isUsing24HourTime = DateFormat.is24HourFormat(context)

        return if (isUsing24HourTime) SimpleDateFormat(pattern.replace("h", "HH").replace(" a".toRegex(), ""), Locale.getDefault())
        else SimpleDateFormat(pattern, Locale.getDefault())
    }

    fun getTimestamp(date: Long): String {
        return getFormatter("h:mm a").format(date)
    }

    fun getMessageTimestamp(date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance()
        then.timeInMillis = date

        return when {
            now.isSameDay(then) -> getFormatter("h:mm a")
            now.isSameWeek(then) -> getFormatter("E h:mm a")
            now.isSameYear(then) -> getFormatter("MMM d, h:mm a")
            else -> getFormatter("MMM d yyyy, h:mm a")
        }.format(date)
    }

    fun getConversationTimestamp(date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance()
        then.timeInMillis = date

        return when {
            now.isSameDay(then) -> getFormatter("h:mm a")
            now.isSameWeek(then) -> getFormatter("E")
            now.isSameYear(then) -> getFormatter("MMM d")
            else -> getFormatter("d/MM/yy")
        }.format(date)
    }

}