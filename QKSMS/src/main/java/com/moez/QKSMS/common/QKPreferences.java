package com.moez.QKSMS.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.moez.QKSMS.enums.QKPreference;

import java.util.Set;

public abstract class QKPreferences {

    private static SharedPreferences sPrefs;

    public static void init(Context context) {
        sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean getBoolean(QKPreference preference) {
        return sPrefs.getBoolean(preference.getKey(), (boolean) preference.getDefaultValue());
    }

    public static void putBoolean(QKPreference preference, boolean newValue) {
        sPrefs.edit().putBoolean(preference.getKey(), newValue).apply();
    }

    public static int getInt(QKPreference preference) {
        return sPrefs.getInt(preference.getKey(), (int) preference.getDefaultValue());
    }

    public static void putInt(QKPreference preference, int newValue) {
        sPrefs.edit().putInt(preference.getKey(), newValue).apply();
    }

    public static long getLong(QKPreference preference) {
        return sPrefs.getLong(preference.getKey(), (int) preference.getDefaultValue());
    }

    public static void putLong(QKPreference preference, long newValue) {
        sPrefs.edit().putLong(preference.getKey(), newValue).apply();
    }

    public static String getString(QKPreference preference) {
        return sPrefs.getString(preference.getKey(), (String) preference.getDefaultValue());
    }

    public static void putString(QKPreference preference, String newValue) {
        sPrefs.edit().putString(preference.getKey(), newValue).apply();
    }

    public static Set<String> getStringSet(QKPreference preference) {
        return sPrefs.getStringSet(preference.getKey(), (Set<String>) preference.getDefaultValue());
    }

    public static void putStringSet(QKPreference preference, Set<String> newValue) {
        sPrefs.edit().putStringSet(preference.getKey(), newValue).apply();
    }
}