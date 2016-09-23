package com.moez.QKSMS.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.moez.QKSMS.R;
import com.moez.QKSMS.mmssms.Transaction;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.util.Calendar;

public class DeliveredReceiver extends BroadcastReceiver {
    private static final String TAG = "DeliveredReceiver";
    private static final boolean LOCAL_LOGV = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LOCAL_LOGV) Log.v(TAG, "marking message as delivered");
        Uri uri;

        try {
            uri = Uri.parse(intent.getStringExtra("message_uri"));

            if (uri.equals("")) {
                uri = null;
            }
        } catch (Exception e) {
            uri = null;
        }

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                // notify user that message was delivered
                Intent delivered = new Intent(Transaction.NOTIFY_OF_DELIVERY);
                delivered.putExtra("result", true);
                delivered.putExtra("message_uri", uri == null ? "" : uri.toString());
                context.sendBroadcast(delivered);

                if (uri != null) {
                    ContentValues values = new ContentValues();
                    values.put("status", "0");
                    values.put("date_sent", Calendar.getInstance().getTimeInMillis());
                    values.put("read", true);
                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                    // mark message as delivered in database
                    if (query.moveToFirst()) {
                        String id = query.getString(query.getColumnIndex("_id"));
                        ContentValues values = new ContentValues();
                        values.put("status", "0");
                        values.put("date_sent", Calendar.getInstance().getTimeInMillis());
                        values.put("read", true);
                        context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                    }

                    query.close();
                }

                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.DELIVERY_TOAST, true)) {
                    Toast.makeText(context, R.string.message_delivered, Toast.LENGTH_LONG).show();
                }

                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.DELIVERY_VIBRATE, true)) {
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);
                }
                break;

            case Activity.RESULT_CANCELED:
                // notify user that message failed to be delivered
                Intent notDelivered = new Intent(Transaction.NOTIFY_OF_DELIVERY);
                notDelivered.putExtra("result", false);
                notDelivered.putExtra("message_uri", uri == null ? "" : uri.toString());
                context.sendBroadcast(notDelivered);

                if (uri != null) {
                    ContentValues values = new ContentValues();
                    values.put("status", "64");
                    values.put("date_sent", Calendar.getInstance().getTimeInMillis());
                    values.put("read", true);
                    values.put("error_code", getResultCode());
                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                    // mark failed in database
                    if (query2.moveToFirst()) {
                        String id = query2.getString(query2.getColumnIndex("_id"));
                        ContentValues values = new ContentValues();
                        values.put("status", "64");
                        values.put("read", true);
                        values.put("error_code", getResultCode());
                        context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                    }

                    query2.close();
                }
                Toast.makeText(context, R.string.message_not_delivered, Toast.LENGTH_LONG).show();
                break;
        }

        context.sendBroadcast(new Intent("com.moez.QKSMS.send_message.REFRESH"));
    }
}