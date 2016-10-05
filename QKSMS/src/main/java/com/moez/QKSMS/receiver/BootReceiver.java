package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.moez.QKSMS.common.NotificationManager;
import com.moez.QKSMS.common.QKPreferences;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.service.DeleteOldMessagesService;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager.initQuickCompose(context, false, false);
        NotificationManager.create(context);

        SettingsFragment.updateAlarmManager(context, QKPreferences.getBoolean(QKPreference.AUTO_NIGHT));

        DeleteOldMessagesService.setupAutoDeleteAlarm(context);
    }
}
