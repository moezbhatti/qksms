package com.moez.QKSMS.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateFormatter {

    public static String getConversationTimestamp(Context context, long date) {
        SharedPreferences prefs = MainActivity.getPrefs(context);
        SimpleDateFormat formatter = new SimpleDateFormat("D, y");

        if (formatter.format(date).equals(formatter.format(System.currentTimeMillis()))) {
            formatter = new SimpleDateFormat(prefs.getBoolean(SettingsFragment.TIMESTAMPS_24H, false) ? "H:mm" : "h:mm a");
        } else {
            formatter = new SimpleDateFormat("w, y");
            if (formatter.format(date).equals(formatter.format(System.currentTimeMillis()))) {
                formatter = new SimpleDateFormat("EEE");
            } else {
                formatter = new SimpleDateFormat("y");
                if (formatter.format(date).equals(formatter.format(System.currentTimeMillis()))) {
                    formatter = new SimpleDateFormat("MMM d");
                } else {
                    formatter = new SimpleDateFormat("MMM d y");
                }
            }
        }

        return formatter.format(date);
    }

    public static String getMessageTimestamp(Context context, long date) {
        SharedPreferences prefs = MainActivity.getPrefs(context);
        boolean isUsing24HourTime = prefs.getBoolean(SettingsFragment.TIMESTAMPS_24H, false);

        SimpleDateFormat formatter = new SimpleDateFormat("D, y");
        if (formatter.format(date).equals(formatter.format(System.currentTimeMillis()))) {
            return new SimpleDateFormat(isUsing24HourTime ? "H:mm" : "h:mm a").format(date);
        }

        // Yesterday
        formatter = new SimpleDateFormat("yD");
        if (Integer.parseInt(formatter.format(date)) + 1 == Integer.parseInt(formatter.format(System.currentTimeMillis()))) {
            return "Yesterday " + new SimpleDateFormat(isUsing24HourTime ? "H:mm" : "h:mm a").format(date);
        }

        // In the same week
        formatter = new SimpleDateFormat("w, y");
        if (formatter.format(date).equals(formatter.format(System.currentTimeMillis()))) {
            return new SimpleDateFormat(isUsing24HourTime ? "EEE H:mm" : "EEE h:mm a").format(date);
        }

        // In the same year
        formatter = new SimpleDateFormat("y");
        if (formatter.format(date).equals(formatter.format(System.currentTimeMillis()))) {
            return new SimpleDateFormat(isUsing24HourTime ? "MMM d, H:mm" : "MMM d, h:mm a").format(date);
        }

        return new SimpleDateFormat(isUsing24HourTime ? "MMM d y, H:mm" : "MMM d y, h:mm a").format(date);
    }

    public static String getDate(Context context, long date) {
        SharedPreferences prefs = MainActivity.getPrefs(context);
        SimpleDateFormat formatter = new SimpleDateFormat(prefs.getBoolean(SettingsFragment.TIMESTAMPS_24H, false) ? "MMMM d y, H:mm:ss" : "MMMM d y, h:mm:ss a");
        return formatter.format(date);
    }

    public static String getRelativeTimestamp(long date) {
        String relativeTimestamp = (String) DateUtils.getRelativeTimeSpanString(date);
        if (relativeTimestamp.equals("in 0 minutes") || relativeTimestamp.equals("0 minutes ago"))
            return "Just now";
        return relativeTimestamp;
    }

    public static String getSummaryTimestamp(Context context, String time) {
        SharedPreferences prefs = MainActivity.getPrefs(context);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm");
        Date date;

        try {
            date = simpleDateFormat.parse(time);
            simpleDateFormat = prefs.getBoolean(SettingsFragment.TIMESTAMPS_24H, false) ? new SimpleDateFormat("H:mm") : new SimpleDateFormat("h:mm a");
            time = simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return time;
    }
}
