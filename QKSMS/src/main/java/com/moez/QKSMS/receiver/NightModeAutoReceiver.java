package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class NightModeAutoReceiver extends BroadcastReceiver {
    private final String TAG = "NightModeAutoReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.getBoolean(SettingsFragment.NIGHT_AUTO, false)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis() + 300000); // add 5 mins in case receiver is called early

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm");
            Calendar day = Calendar.getInstance();
            Calendar night = Calendar.getInstance();
            try {
                day.setTime(simpleDateFormat.parse(prefs.getString(SettingsFragment.DAY_START, "6:00")));
                night.setTime(simpleDateFormat.parse(prefs.getString(SettingsFragment.NIGHT_START, "21:00")));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if ((calendar.get(Calendar.HOUR_OF_DAY) > night.get(Calendar.HOUR_OF_DAY)) ||
                    (calendar.get(Calendar.HOUR_OF_DAY) == night.get(Calendar.HOUR_OF_DAY) && calendar.get(Calendar.MINUTE) >= night.get(Calendar.MINUTE)) ||
                    (calendar.get(Calendar.HOUR_OF_DAY) < day.get(Calendar.HOUR_OF_DAY)) ||
                    (calendar.get(Calendar.HOUR_OF_DAY) == day.get(Calendar.HOUR_OF_DAY) && calendar.get(Calendar.MINUTE) <= day.get(Calendar.MINUTE))) {
                Log.i(TAG, "Switching to night mode");
                prefs.edit().putString(SettingsFragment.BACKGROUND, ThemeManager.Theme.PREF_GREY).apply();
                ThemeManager.setTheme(ThemeManager.Theme.DARK);
            } else {
                Log.i(TAG, "Switching to day mode");
                prefs.edit().putString(SettingsFragment.BACKGROUND, ThemeManager.Theme.PREF_OFFWHITE).apply();
                ThemeManager.setTheme(ThemeManager.Theme.LIGHT);
            }
        }
    }
}
