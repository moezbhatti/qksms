package com.moez.QKSMS;

import com.crittercism.app.Crittercism;

public class QKSMSApp_ extends QKSMSApp {
    @Override
    public void onCreate() {
        super.onCreate();
        Crittercism.initialize(getApplicationContext(), getString(R.string.crtsm_key));
    }
}
