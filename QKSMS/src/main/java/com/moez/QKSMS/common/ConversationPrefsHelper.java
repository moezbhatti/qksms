package com.moez.QKSMS.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class ConversationPrefsHelper {

    public static final String CONVERSATIONS_FILE = "conversation_";

    private Context mContext;
    private SharedPreferences mPrefs;
    private SharedPreferences mConversationPrefs;

    public ConversationPrefsHelper(Context context, long threadId) {
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mConversationPrefs = context.getSharedPreferences(CONVERSATIONS_FILE + threadId, Context.MODE_PRIVATE);
    }

    public int getColor() {
        return Integer.parseInt(mConversationPrefs.getString(SettingsFragment.THEME, "" + ThemeManager.getThemeColor()));
    }

    public boolean getNotificationsEnabled() {
        return getBoolean(SettingsFragment.NOTIFICATIONS, true);
    }

    public boolean getNotificationLedEnabled() {
        return getBoolean(SettingsFragment.NOTIFICATION_LED, true);
    }

    public String getNotificationLedColor() {
        return getString(SettingsFragment.NOTIFICATION_LED_COLOR, "" + mContext.getResources().getColor(R.color.red_light));
    }

    public boolean getWakePhoneEnabled() {
        return getBoolean(SettingsFragment.WAKE, false);
    }

    public boolean getTickerEnabled() {
        return getBoolean(SettingsFragment.NOTIFICATION_TICKER, true);
    }

    public Integer getPrivateNotificationsSetting(){
        return Integer.parseInt(mPrefs.getString(SettingsFragment.PRIVATE_NOTIFICATION, "0"));
    }

    public boolean getVibrateEnabled() {
        return getBoolean(SettingsFragment.NOTIFICATION_VIBRATE, true);
    }

    public String getNotificationSound() {
        return getString(SettingsFragment.NOTIFICATION_TONE, SettingsFragment.DEFAULT_NOTIFICATION_TONE);
    }

    public Uri getNotificationSoundUri() {
        return Uri.parse(getNotificationSound());
    }

    public boolean getCallButtonEnabled() {
        return getBoolean(SettingsFragment.NOTIFICATION_CALL_BUTTON, false);
    }

    public boolean getDimissedReadEnabled() {
        return getBoolean(SettingsFragment.DISMISSED_READ, false);
    }

    public void putInt(String key, int value) {
        mConversationPrefs.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        int globalValue = mPrefs.getInt(key, defaultValue);
        return mConversationPrefs.getInt(key, globalValue);
    }

    public void putString(String key, String value) {
        mConversationPrefs.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        String globalValue = mPrefs.getString(key, defaultValue);
        return mConversationPrefs.getString(key, globalValue);
    }

    public void putBoolean(String key, boolean value) {
        mConversationPrefs.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        boolean globalValue = mPrefs.getBoolean(key, defaultValue);
        return mConversationPrefs.getBoolean(key, globalValue);
    }

    public SharedPreferences getConversationPrefs() {
        return mConversationPrefs;
    }
}
