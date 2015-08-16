package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.moez.QKSMS.transaction.NotificationManager;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager.initQuickCompose(context, false, false);
        NotificationManager.create(context);

        SettingsFragment.updateAlarmManager(context, MainActivity.getPrefs(context).getBoolean(SettingsFragment.NIGHT_AUTO, false));
    }
}
