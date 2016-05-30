package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

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

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ContentValues values = new ContentValues();
                values.put("read", true);
                context.getContentResolver().update(mMessageUri, values, null, null);
            }

            protected Void doInBackground(Void... none) {
                context.getContentResolver().delete(mMessageUri, null, null);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                NotificationManager.dismiss(context, (int) threadId);
            }
        }.execute();


    }
}
