package blocking;

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
                    boolean enabled = msg.getData().getBoolean("enabled");
                    prefs.edit().putBoolean("pref_key_should_i_answer", enabled).apply();
                }

                data.putBoolean("blocking_enabled", prefs.getBoolean("pref_key_should_i_answer", false));

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
