package com.moez.QKSMS;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class QKSMSApp extends QKSMSAppBase {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }
}
