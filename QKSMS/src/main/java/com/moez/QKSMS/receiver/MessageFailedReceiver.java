package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.moez.QKSMS.R;
import com.moez.QKSMS.transaction.NotificationManager;

public class MessageFailedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, R.string.toast_message_failure, Toast.LENGTH_LONG).show();
        NotificationManager.notifyFailed(context);
    }
}
