package com.moez.QKSMS.common.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class QKPreferences {

    private SharedPreferences mPrefs;

    public QKPreferences(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getBoolean(QKPreference preference) {
        return mPrefs.getBoolean(preference.getKey(), (boolean) preference.getDefaultValue());
    }

    public void setBoolean(QKPreference preference, boolean newValue) {
        mPrefs.edit().putBoolean(preference.getKey(), newValue).apply();
    }

    public int getInt(QKPreference preference) {
        return mPrefs.getInt(preference.getKey(), (int) preference.getDefaultValue());
    }

    public void setInt(QKPreference preference, int newValue) {
        mPrefs.edit().putInt(preference.getKey(), newValue).apply();
    }

    public String getString(QKPreference preference) {
        return mPrefs.getString(preference.getKey(), (String) preference.getDefaultValue());
    }

    public void setString(QKPreference preference, String newValue) {
        mPrefs.edit().putString(preference.getKey(), newValue).apply();
    }
}