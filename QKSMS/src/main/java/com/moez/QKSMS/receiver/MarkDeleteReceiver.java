package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.moez.QKSMS.data.ConversationLegacy;
import com.moez.QKSMS.transaction.NotificationManager;

/**
 * Created by vijaysy on 27/05/16.
 */
public class MarkDeleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        long threadId = extras.getLong("thread_id");
        Uri mMessageUri = extras.getParcelable("mMessageUri");
        new Thread(() -> {
            ConversationLegacy conversation = new ConversationLegacy(context, threadId);
            conversation.markRead();
            context.getContentResolver().delete(mMessageUri, null, null);
            NotificationManager.dismiss(context, (int) threadId);
        }).start();

      /*
      new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... none) {
                try {
                    Log.i("Deleting SMS from inbox", "");
                    context.getContentResolver().delete(mMessageUri, null, null);
                } catch (Exception e) {
                    Log.i("Could not delete", e.getMessage());
                }
                NotificationManager.dismiss(context, (int) thread_id);
                return null;
            }
        }.execute();
      */


    }
}
