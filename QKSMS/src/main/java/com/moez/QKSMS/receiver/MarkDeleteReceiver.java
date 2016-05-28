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
        ConversationLegacy conversation = new ConversationLegacy(context, threadId);
        conversation.markRead();
        new Thread(() -> {
            context.getContentResolver().delete(mMessageUri, null, null);
            NotificationManager.dismiss(context, (int) threadId);
        }).start();


//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                ConversationLegacy conversation = new ConversationLegacy(context, threadId);
//                conversation.markRead();
//            }
//
//            protected Void doInBackground(Void... none) {
//                context.getContentResolver().delete(mMessageUri, null, null);
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                NotificationManager.dismiss(context, (int) threadId);
//                QKReplyActivity.dismiss(threadId);
//            }
//        }.execute();


    }
}
