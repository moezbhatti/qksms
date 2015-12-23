package org.mistergroup.muzutozvednout;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * Created by Mister Group s.r.o. on 30.11.2015.
 */
public class ShouldIAnswerBinder {
    public static final int RATING_UNKNOWN = 0;
    public static final int RATING_POSITIVE = 1;
    public static final int RATING_NEGATIVE = 2;
    public static final int RATING_NEUTRAL = 3;

    private Callback callback;
    public boolean isBound;

    public interface Callback {
        void onNumberRating(String number, int rating);

        void onServiceConnected();

        void onServiceDisconnected();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void bind(Context context) {
        Intent intent=new Intent("org.mistergroup.muzutozvednout.PublicService");
        intent.setPackage("org.mistergroup.muzutozvednout");
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbind(Context context) {
        if ((isBound) && (serviceMessenger != null)) {
            context.unbindService(mConnection);
        }
    }

    public void getNumberRating(String number) throws RemoteException {
        Message msg = new Message();
        msg.what = 1;
        Bundle data = new Bundle();
        data.putString("number", number);
        msg.setData(data);
        msg.replyTo = messenger;
        serviceMessenger.send(msg);
    }

    private final Messenger messenger = new Messenger(new IncomingHandler());
    private static Messenger serviceMessenger = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            serviceMessenger = new Messenger(service);
            isBound = true;
            if (callback != null)
                callback.onServiceConnected();
        }

        public void onServiceDisconnected(ComponentName className) {
            serviceMessenger = null;
            isBound = false;
            if (callback != null)
                callback.onServiceDisconnected();
        }
    };

    private class IncomingHandler extends Handler {
        static final int GET_NUMBER_RATING = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_NUMBER_RATING:
                    Bundle inData = msg.getData();
                    String number = inData.getString("number");
                    int rating = inData.getInt("rating");
                    if (callback != null)
                        callback.onNumberRating(number, rating);

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
