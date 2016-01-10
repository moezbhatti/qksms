package com.moez.QKSMS.common;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * http://stackoverflow.com/questions/3667022/checking-if-an-android-application-is-running-in-the-background/
 */
public class LifecycleHandler implements Application.ActivityLifecycleCallbacks {
    private final String TAG = "LifecycleHandler";

    private static int sResumed;
    private static int sPaused;
    private static int sStarted;
    private static int sStopped;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        sResumed++;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        sPaused++;
        Log.i("test", "application is in foreground: " + (sResumed > sPaused));
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        sStarted++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        sStopped++;
        Log.i("test", "application is visible: " + (sStarted > sStopped));
    }

    public static boolean isApplicationVisible() {
        return sStarted > sStopped;
    }

    public static boolean isApplicationInForeground() {
        return sResumed > sPaused;
    }
}