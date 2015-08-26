package com.moez.QKSMS.common;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.moez.QKSMS.R;

public class AnalyticsManager implements AnalyticsManagerBase {
    private AnalyticsManager() {
    }

    private static class AnalyticsManagerHolder {
        private final static AnalyticsManager INSTANCE = new AnalyticsManager();
    }

    public static AnalyticsManager getInstance() {
        return AnalyticsManagerHolder.INSTANCE;
    }

    private boolean mNeedsInit = true;
    private Context mContext;
    private Tracker mTracker;


    public void init(Context context) {
        if (LOCAL_LOGV) Log.v(TAG, "init called. mNeedsInit: " + mNeedsInit);

        if (mNeedsInit) {
            mNeedsInit = false;
            mContext = context;

            // Initialize tracker
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);
            mTracker = analytics.newTracker(R.xml.google_analytics_tracker);
        }
    }

    public void sendEvent(String category, String action, String label) {
        if (LOCAL_LOGV) Log.v(TAG, "sendEvent category:" + category + ", action:" + action +
                ", label:" + label);

        HitBuilders.EventBuilder b = new HitBuilders.EventBuilder();
        if (category != null) b.setCategory(category);
        if (action != null) b.setAction(action);
        if (label != null) b.setLabel(label);

        mTracker.send(b.build());
    }

    public void sendEvent(String category, String action, String label, long value) {
        if (LOCAL_LOGV) Log.v(TAG, "sendEvent category:" + category + ", action:" + action +
                ", label:" + label + ", value:" + value);

        HitBuilders.EventBuilder b = new HitBuilders.EventBuilder();
        if (category != null) b.setCategory(category);
        if (action != null) b.setAction(action);
        if (label != null) b.setLabel(label);
        b.setValue(value);

        mTracker.send(b.build());
    }
}
