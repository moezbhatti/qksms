package com.moez.QKSMS.common;

import android.content.Context;

interface AnalyticsManagerBase {
    String TAG = "AnalyticsManager";
    boolean LOCAL_LOGV = false;
    String CATEGORY_MESSAGES = "messages";
    // Note: For preferences events, the action will just be the preference value.
    String CATEGORY_PREFERENCE_CHANGE = "preference_change";
    String CATEGORY_PREFERENCE_CLICK = "preference_click";
    String CATEGORY_REPORT = "report";
    String ACTION_SEND_MESSAGE = "send_message";
    String ACTION_ATTACH_IMAGE = "attach_image";
    String ACTION_ATTACH_FROM_CAMERA = "attach_from_camera";

    void init(Context context);

    void sendEvent(String category, String action, String label);

    void sendEvent(String category, String action, String label, long value);
}
