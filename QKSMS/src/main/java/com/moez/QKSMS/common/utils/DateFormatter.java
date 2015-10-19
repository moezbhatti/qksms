package com.moez.QKSMS.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import com.moez.QKSMS.QKSMSApp;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateFormatter {

    public static String getConversationTimestamp(Context context, long date) {
        if (isSameDay(date)) {
            return accountFor24HourTime(context, new SimpleDateFormat("h:mm a")).format(date);
        } else if (isSameWeek(date)) {
            return DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY);
        } else if (isSameYear(date)) {
            return DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH);
        } else {
            return DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
        }
    }

    private static boolean isSameDay(long date) {
        SimpleDateFormat formatter = new SimpleDateFormat("D, y");
        return formatter.format(date).equals(formatter.format(System.currentTimeMillis()));
    }

    private static boolean isSameWeek(long date) {
        SimpleDateFormat formatter = new SimpleDateFormat("w, y");
        return formatter.format(date).equals(formatter.format(System.currentTimeMillis()));
    }

    private static boolean isSameYear(long date) {
        SimpleDateFormat formatter = new SimpleDateFormat("y");
        return formatter.format(date).equals(formatter.format(System.currentTimeMillis()));
    }

    private static boolean isYesterday(long date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yD");
        return Integer.parseInt(formatter.format(date)) + 1 == Integer.parseInt(formatter.format(System.currentTimeMillis()));
    }

    public static SimpleDateFormat accountFor24HourTime(Context context, SimpleDateFormat input) { //pass in 12 hour time. If needed, change to 24 hr.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isUsing24HourTime = prefs.getBoolean(SettingsFragment.TIMESTAMPS_24H, DateFormat.is24HourFormat(context));

        if (isUsing24HourTime) {
            return new SimpleDateFormat(input.toPattern().replace('h', 'H').replaceAll(" a", ""));
        } else return input;
    }

    public static String getMessageTimestamp(Context context, long date) {
        String time = ", " + accountFor24HourTime(context, new SimpleDateFormat("h:mm a")).format(date);
        if (isSameDay(date)) {
            return accountFor24HourTime(context, new SimpleDateFormat("h:mm a")).format(date);
        } else if (isYesterday(date)) {
            return context.getString(R.string.date_yesterday) + time;
        } else if (isSameWeek(date)) {
            return DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY) + time;
        } else if (isSameYear(date)) {
            return DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH) + time;
        }

        return DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH) + time;
    }

    public static String getDate(Context context, long date) {
        return DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_DATE) + accountFor24HourTime(context, new SimpleDateFormat(", h:mm:ss a")).format(date);
    }

    public static String getRelativeTimestamp(long date) {
        String relativeTimestamp = (String) DateUtils.getRelativeTimeSpanString(date);
        if (relativeTimestamp.equals("in 0 minutes") || relativeTimestamp.equals("0 minutes ago"))
            return QKSMSApp.getApplication().getString(com.moez.QKSMS.R.string.date_just_now);
        return relativeTimestamp;
    }

    public static String getSummaryTimestamp(Context context, String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm");
        Date date;

        try {
            date = simpleDateFormat.parse(time);
            simpleDateFormat = accountFor24HourTime(context, new SimpleDateFormat("H:mm"));
            time = simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return time;
    }
}
