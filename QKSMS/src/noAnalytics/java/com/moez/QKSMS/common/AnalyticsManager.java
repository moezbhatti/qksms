package com.moez.QKSMS.common;

import android.content.Context;

public enum AnalyticsManager {
    INSTANCE;

    private final static String TAG = "AnalyticsManager";
    private final static boolean LOCAL_LOGV = false;
    public final static String CATEGORY_MESSAGES = "messages";
    // Note: For preferences events, the action will just be the preference value.
    public final static String CATEGORY_PREFERENCE_CHANGE = "preference_change";
    public final static String CATEGORY_PREFERENCE_CLICK = "preference_click";
    public final static String CATEGORY_REPORT = "report";
    public final static String ACTION_SEND_MESSAGE = "send_message";
    public final static String ACTION_ATTACH_IMAGE = "attach_image";
    public final static String ACTION_ATTACH_FROM_CAMERA = "attach_from_camera";

    public static AnalyticsManager getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
    }

    public void sendEvent(String category, String action, String label) {
    }

    public void sendEvent(String category, String action, String label, long value) {
    }
}
