package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The QKSMS icon color can be changed. When it's changed, the QKSMS app must be closed. This
 * Receiver received the system broadcast that a package has changed, and re-launches QKSMS.
 */
public class IconColorReceiver extends BroadcastReceiver {
    private static final String TAG = "IconColorReceiver";
    private static final boolean LOCAL_LOGV = false;

    /**
     * Broadcast this intent when the user has changed the icon.
     */
    public static final String ACTION_ICON_COLOR_CHANGED =
            "com.moez.QKSMS.action.PENDING_PACKAGE_CHANGE";

    /**
     * When ACTION_ICON_COLOR_CHANGED is broadcast, make sure to add the component name to the
     * intent so that we can restart the app here.
     */
    public static final String EXTRA_COMPONENT_NAME =
            "com.moez.QKSMS.extra.EXTRA_COMPONENT_NAME";

    /**
     * Non-null to specify the activity we need to start when we next receive an
     * ACTION_PACKAGE_CHANGED broadcast.
     */
    public static final String PREF_PENDING_COMPONENT =
            "ColorIconReceiver:pending_activity_start";

    /**
     * This action is also used as the action to start the component specified in
     * EXTRA_COMPONENT_NAME, so that it knows it was started in the context of "the icon has just
     * changed". This is used, ex., in MainActivity to show a dialog saying "Check out your new
     * icon!"
     */
    public static final String EXTRA_ICON_COLOR_CHANGED =
            "com.moez.QKSMS.extra.EXTRA_ICON_COLOR_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        intent = intent == null ? new Intent() : intent;
        if (LOCAL_LOGV) Log.v(TAG, "[" + this + "] onReceive: " + intent);

        if (Intent.ACTION_PACKAGE_CHANGED.equals(intent.getAction())) {

            // When we receive the PACKAGE_CHANGED event, check if it was our package that changed.
            // If so, launch the app now.
            String component = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(PREF_PENDING_COMPONENT, null);
            if (component != null) {
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit()
                        .putString(PREF_PENDING_COMPONENT, null)
                        .commit();

                Intent activity = new Intent(ACTION_ICON_COLOR_CHANGED);
                activity.setComponent(new ComponentName(context, component));
                activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.putExtra(EXTRA_ICON_COLOR_CHANGED, true);
                context.startActivity(activity);
            }

        } else if (ACTION_ICON_COLOR_CHANGED.equals(intent.getAction())) {
            String component = intent.getStringExtra(EXTRA_COMPONENT_NAME);

            // Save the component class name for starting when we getConversation the PACKAGE_CHANGED_ACTION
            // broadcast.
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(PREF_PENDING_COMPONENT, component)
                    .commit();

            // Enable the new color and kill the app.
            PackageManager packageManager = context.getPackageManager();
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context, component),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    0 // no flags---i.e. no "DONT_KILL_APP" flag
            );
        }
    }
}
