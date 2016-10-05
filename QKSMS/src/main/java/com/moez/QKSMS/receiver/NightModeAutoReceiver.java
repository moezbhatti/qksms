package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.moez.QKSMS.common.QKPreferences;
import com.moez.QKSMS.common.ThemeManager;
import com.moez.QKSMS.enums.QKPreference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class NightModeAutoReceiver extends BroadcastReceiver {
    private final String TAG = "NightModeAutoReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (QKPreferences.getBoolean(QKPreference.AUTO_NIGHT)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis() + 300000); // add 5 mins in case receiver is called early

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm");
            Calendar day = Calendar.getInstance();
            Calendar night = Calendar.getInstance();
            try {
                day.setTime(simpleDateFormat.parse(QKPreferences.getString(QKPreference.AUTO_NIGHT_DAY_START)));
                night.setTime(simpleDateFormat.parse(QKPreferences.getString(QKPreference.AUTO_NIGHT_NIGHT_START)));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if ((calendar.get(Calendar.HOUR_OF_DAY) > night.get(Calendar.HOUR_OF_DAY)) ||
                    (calendar.get(Calendar.HOUR_OF_DAY) == night.get(Calendar.HOUR_OF_DAY) && calendar.get(Calendar.MINUTE) >= night.get(Calendar.MINUTE)) ||
                    (calendar.get(Calendar.HOUR_OF_DAY) < day.get(Calendar.HOUR_OF_DAY)) ||
                    (calendar.get(Calendar.HOUR_OF_DAY) == day.get(Calendar.HOUR_OF_DAY) && calendar.get(Calendar.MINUTE) <= day.get(Calendar.MINUTE))) {
                Log.i(TAG, "Switching to night mode");
                QKPreferences.putString(QKPreference.BACKGROUND, ThemeManager.Theme.PREF_GREY);
                ThemeManager.setTheme(ThemeManager.Theme.DARK);
            } else {
                Log.i(TAG, "Switching to day mode");
                QKPreferences.putString(QKPreference.BACKGROUND, ThemeManager.Theme.PREF_OFFWHITE);
                ThemeManager.setTheme(ThemeManager.Theme.LIGHT);
            }
        }
    }
}
