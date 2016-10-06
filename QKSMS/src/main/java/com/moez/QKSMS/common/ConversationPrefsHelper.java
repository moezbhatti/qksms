package com.moez.QKSMS.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import com.moez.QKSMS.enums.QKPreference;

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
        return Integer.parseInt(mConversationPrefs.getString(QKPreference.THEME.getKey(), "" + ThemeManager.getThemeColor()));
    }

    public boolean getNotificationsEnabled() {
        return getBoolean(QKPreference.NOTIFICATIONS);
    }

    public boolean getNotificationLedEnabled() {
        return getBoolean(QKPreference.NOTIFICATIONS_LED);
    }

    public String getNotificationLedColor() {
        return getString(QKPreference.NOTIFICATIONS_LED_COLOR);
    }

    public boolean getWakePhoneEnabled() {
        return getBoolean(QKPreference.NOTIFICATIONS_WAKE);
    }

    public boolean getTickerEnabled() {
        return getBoolean(QKPreference.NOTIFICATIONS_TICKER);
    }

    public Integer getPrivateNotificationsSetting(){
        return Integer.parseInt(getString(QKPreference.NOTIFICATIONS_PRIVATE));
    }

    public boolean getVibrateEnabled() {
        return getBoolean(QKPreference.NOTIFICATIONS_VIBRATION);
    }

    public String getNotificationSound() {
        return getString(QKPreference.NOTIFICATIONS_SOUND);
    }

    public Uri getNotificationSoundUri() {
        return Uri.parse(getNotificationSound());
    }

    public boolean getCallButtonEnabled() {
        return getBoolean(QKPreference.NOTIFICATIONS_CALL_BUTTON);
    }

    public boolean getDimissedReadEnabled() {
        return getBoolean(QKPreference.NOTIFICATIONS_MARK_READ);
    }

    public void putInt(QKPreference preference, int value) {
        mConversationPrefs.edit().putInt(preference.getKey(), value).apply();
    }

    private int getInt(QKPreference preference) {
        int globalValue = QKPreferences.getInt(preference);
        return mConversationPrefs.getInt(preference.getKey(), globalValue);
    }

    public void putString(QKPreference preference, String value) {
        mConversationPrefs.edit().putString(preference.getKey(), value).apply();
    }

    private String getString(QKPreference preference) {
        String globalValue = QKPreferences.getString(preference);
        return mConversationPrefs.getString(preference.getKey(), globalValue);
    }

    public void putBoolean(QKPreference preference, boolean value) {
        mConversationPrefs.edit().putBoolean(preference.getKey(), value).apply();
    }

    private boolean getBoolean(QKPreference preference) {
        boolean globalValue = QKPreferences.getBoolean(preference);
        return mConversationPrefs.getBoolean(preference.getKey(), globalValue);
    }

    public SharedPreferences getConversationPrefs() {
        return mConversationPrefs;
    }
}
