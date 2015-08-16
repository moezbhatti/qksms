/*
 * Copyright (C) 2015 QK Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moez.QKSMS.mmssms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

public class SentReceiver extends BroadcastReceiver {
    private static final String TAG = "SentReceiver";
    private static final boolean LOCAL_LOGV = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LOCAL_LOGV) Log.v(TAG, "marking message as sent");
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
                if (uri != null) {
                    try {
                        if (LOCAL_LOGV) Log.v(TAG, "using supplied uri");
                        ContentValues values = new ContentValues();
                        values.put("type", 2);
                        values.put("read", 1);
                        context.getContentResolver().update(uri, values, null, null);
                    } catch (NullPointerException e) {
                        markFirstAsSent(context);
                    }
                } else {
                    markFirstAsSent(context);
                }

                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            case SmsManager.RESULT_ERROR_NO_SERVICE:
            case SmsManager.RESULT_ERROR_NULL_PDU:
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                if (uri != null) {
                    if (LOCAL_LOGV) Log.v(TAG, "using supplied uri");
                    ContentValues values = new ContentValues();
                    values.put("type", 5);
                    values.put("read", true);
                    values.put("error_code", getResultCode());
                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    if (LOCAL_LOGV) Log.v(TAG, "using first message");
                    Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                    // mark message failed
                    if (query != null && query.moveToFirst()) {
                        String id = query.getString(query.getColumnIndex("_id"));
                        ContentValues values = new ContentValues();
                        values.put("type", 5);
                        values.put("read", 1);
                        values.put("error_code", getResultCode());
                        context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                    }
                }

                context.sendBroadcast(new Intent(Transaction.NOTIFY_SMS_FAILURE));

                break;
        }

        context.sendBroadcast(new Intent("com.moez.QKSMS.send_message.REFRESH"));
    }

    private void markFirstAsSent(Context context) {
        if (LOCAL_LOGV) Log.v(TAG, "using first message");
        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

        // mark message as sent successfully
        if (query != null && query.moveToFirst()) {
            String id = query.getString(query.getColumnIndex("_id"));
            ContentValues values = new ContentValues();
            values.put("type", 2);
            values.put("read", 1);
            context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
        }

        query.close();
    }
}
