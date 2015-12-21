package com.moez.QKSMS.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class ShouldIAnswerService extends Service {

    private Messenger mMessenger = new Messenger(new BlockingEnabledHandler(this));

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private class BlockingEnabledHandler extends Handler {
        private Context mContext;

        private BlockingEnabledHandler(Context context) {
            mContext = context;
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Messenger messenger = msg.replyTo;
            if (messenger != null && (msg.what == 918 || msg.what == 919)) {
                Bundle data = new Bundle();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                if (msg.what == 919) {
                    boolean enabled=msg.getData().getBoolean("enabled");
                    prefs.edit().putBoolean(SettingsFragment.SHOULD_I_ANSWER, enabled).commit();
                }

                data.putBoolean("blocking_enabled", prefs.getBoolean(SettingsFragment.SHOULD_I_ANSWER, false));

                Message message = new Message();
                message.what = msg.what;
                message.setData(data);

                try {
                    messenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
