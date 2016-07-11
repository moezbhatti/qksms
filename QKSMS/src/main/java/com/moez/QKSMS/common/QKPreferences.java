package com.moez.QKSMS.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.moez.QKSMS.enums.QKPreference;

public abstract class QKPreferences {

    private static SharedPreferences sPrefs;

    public static void init(Context context) {
        sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean getBoolean(QKPreference preference) {
        return sPrefs.getBoolean(preference.getKey(), (boolean) preference.getDefaultValue());
    }

    public static void setBoolean(QKPreference preference, boolean newValue) {
        sPrefs.edit().putBoolean(preference.getKey(), newValue).apply();
    }

    public static int getInt(QKPreference preference) {
        return sPrefs.getInt(preference.getKey(), (int) preference.getDefaultValue());
    }

    public static void setInt(QKPreference preference, int newValue) {
        sPrefs.edit().putInt(preference.getKey(), newValue).apply();
    }

    public static long getLong(QKPreference preference) {
        return sPrefs.getLong(preference.getKey(), (int) preference.getDefaultValue());
    }

    public static void setLong(QKPreference preference, long newValue) {
        sPrefs.edit().putLong(preference.getKey(), newValue).apply();
    }

    public static String getString(QKPreference preference) {
        return sPrefs.getString(preference.getKey(), (String) preference.getDefaultValue());
    }

    public static void setString(QKPreference preference, String newValue) {
        sPrefs.edit().putString(preference.getKey(), newValue).apply();
    }
}