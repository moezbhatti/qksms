package com.moez.QKSMS.receiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class DeliveredReceiver extends com.moez.QKSMS.mmssms.DeliveredReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.DELIVERY_TOAST, true)) {
                    Toast.makeText(context, R.string.message_delivered, Toast.LENGTH_LONG).show();
                }

                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.DELIVERY_VIBRATE, true)) {
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);
                }
                break;

            case Activity.RESULT_CANCELED:
                Toast.makeText(context, R.string.message_not_delivered, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
