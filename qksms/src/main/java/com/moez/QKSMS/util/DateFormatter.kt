package com.moez.QKSMS.util

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.moez.QKSMS.R
import java.text.SimpleDateFormat
import java.util.*

class DateFormatter(val context: Context) {

    /**
     * Replace 12 hour format with 24 hour format if necessary
     */
    private fun accountFor24HourTime(context: Context, input: SimpleDateFormat): SimpleDateFormat {
        val isUsing24HourTime = DateFormat.is24HourFormat(context)

        return if (isUsing24HourTime) SimpleDateFormat(input.toPattern().replace('h', 'H').replace(" a".toRegex(), ""))
        else input
    }

    fun getMessageTimestamp(date: Long): String {
        val then = Calendar.getInstance()
        then.timeInMillis = date

        val now = Calendar.getInstance()

        val time = ", " + accountFor24HourTime(context, SimpleDateFormat("h:mm a")).format(date)
        return when {
            now.isSameDay(then) -> accountFor24HourTime(context, SimpleDateFormat("h:mm a")).format(date)
            now.isDayAfter(then) -> context.getString(R.string.date_yesterday) + time
            now.isSameWeek(then) -> DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_WEEKDAY) + time
            now.isSameYear(then) -> DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_YEAR or DateUtils.FORMAT_ABBREV_MONTH) + time
            else -> DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH) + time
        }
    }

}