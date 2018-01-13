/*
 * Copyright 2014 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.transaction;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.MmsSms.PendingMessages;

import com.android.mms.logs.LogTag;
import com.android.mms.util.DownloadManager;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduPersister;
import com.klinker.android.logger.Log;
import com.klinker.android.send_message.BroadcastUtils;
import com.klinker.android.send_message.R;

public class RetryScheduler implements Observer {
    private static final String TAG = LogTag.TAG;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private final Context mContext;
    private final ContentResolver mContentResolver;

    private RetryScheduler(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    private static RetryScheduler sInstance;
    public static RetryScheduler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RetryScheduler(context);
        }
        return sInstance;
    }

    private boolean isConnected() {
        ConnectivityManager mConnMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
        return (ni == null ? false : ni.isConnected());
    }

    public void update(Observable observable) {
        try {
            Transaction t = (Transaction) observable;

            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "[RetryScheduler] update " + observable);
            }

            // We are only supposed to handle M-Notification.ind, M-Send.req
            // and M-ReadRec.ind.
            if ((t instanceof NotificationTransaction)
                    || (t instanceof RetrieveTransaction)
                    || (t instanceof ReadRecTransaction)
                    || (t instanceof SendTransaction)) {
                try {
                    TransactionState state = t.getState();
                    if (state.getState() == TransactionState.FAILED) {
                        Uri uri = state.getContentUri();
                        if (uri != null) {
                            scheduleRetry(uri);
                        }
                    }
                } finally {
                    t.detach(this);
                }
            }
        } finally {
            if (isConnected()) {
                setRetryAlarm(mContext);
            }
        }
    }

    private void scheduleRetry(Uri uri) {
        long msgId = ContentUris.parseId(uri);

        Uri.Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        uriBuilder.appendQueryParameter("message", String.valueOf(msgId));

        Cursor cursor = SqliteWrapper.query(mContext, mContentResolver,
                uriBuilder.build(), null, null, null, null);

        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    int msgType = cursor.getInt(cursor.getColumnIndexOrThrow(
                            PendingMessages.MSG_TYPE));

                    int retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow(
                            PendingMessages.RETRY_INDEX)) + 1; // Count this time.

                    // TODO Should exactly understand what was happened.
                    int errorType = MmsSms.ERR_TYPE_GENERIC;

                    DefaultRetryScheme scheme = new DefaultRetryScheme(mContext, retryIndex);

                    ContentValues values = new ContentValues(4);
                    long current = System.currentTimeMillis();
                    boolean isRetryDownloading =
                            (msgType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND);
                    boolean retry = true;
                    int respStatus = getResponseStatus(msgId);
                    int errorString = 0;
                    if (!isRetryDownloading) {
                        // Send Transaction case
                        switch (respStatus) {
                            case PduHeaders.RESPONSE_STATUS_ERROR_SENDING_ADDRESS_UNRESOLVED:
                                errorString = R.string.invalid_destination;
                                break;
                            case PduHeaders.RESPONSE_STATUS_ERROR_SERVICE_DENIED:
                            case PduHeaders.RESPONSE_STATUS_ERROR_PERMANENT_SERVICE_DENIED:
                                errorString = R.string.service_not_activated;
                                break;
                            case PduHeaders.RESPONSE_STATUS_ERROR_NETWORK_PROBLEM:
                                errorString = R.string.service_network_problem;
                                break;
                            case PduHeaders.RESPONSE_STATUS_ERROR_TRANSIENT_MESSAGE_NOT_FOUND:
                            case PduHeaders.RESPONSE_STATUS_ERROR_PERMANENT_MESSAGE_NOT_FOUND:
                                errorString = R.string.service_message_not_found;
                                break;
                        }
                        if (errorString != 0) {
                            DownloadManager.init(mContext.getApplicationContext());
                            DownloadManager.getInstance().showErrorCodeToast(errorString);
                            retry = false;
                        }
                    } else {
                        // apply R880 IOT issue (Conformance 11.6 Retrieve Invalid Message)
                        // Notification Transaction case
                        respStatus = getRetrieveStatus(msgId);
                        if (respStatus ==
                                PduHeaders.RESPONSE_STATUS_ERROR_PERMANENT_MESSAGE_NOT_FOUND) {
                            DownloadManager.init(mContext.getApplicationContext());
                            DownloadManager.getInstance().showErrorCodeToast(
                                    R.string.service_message_not_found);
                            SqliteWrapper.delete(mContext, mContext.getContentResolver(), uri,
                                    null, null);
                            retry = false;
                            return;
                        }
                    }
                    if ((retryIndex < scheme.getRetryLimit()) && retry) {
                        long retryAt = current + scheme.getWaitingInterval();

                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "scheduleRetry: retry for " + uri + " is scheduled at "
                                    + (retryAt - System.currentTimeMillis()) + "ms from now");
                        }

                        values.put(PendingMessages.DUE_TIME, retryAt);

                        if (isRetryDownloading) {
                            // Downloading process is transiently failed.
                            DownloadManager.init(mContext.getApplicationContext());
                            DownloadManager.getInstance().markState(
                                    uri, DownloadManager.STATE_TRANSIENT_FAILURE);
                        }
                    } else {
                        errorType = MmsSms.ERR_TYPE_GENERIC_PERMANENT;
                        if (isRetryDownloading) {
                            Cursor c = SqliteWrapper.query(mContext, mContext.getContentResolver(), uri,
                                    new String[] { Mms.THREAD_ID }, null, null, null);

                            long threadId = -1;
                            if (c != null) {
                                try {
                                    if (c.moveToFirst()) {
                                        threadId = c.getLong(0);
                                    }
                                } finally {
                                    c.close();
                                }
                            }

                            if (threadId != -1) {
                                // Downloading process is permanently failed.
                                markMmsFailed(mContext);
                            }

                            DownloadManager.init(mContext.getApplicationContext());
                            DownloadManager.getInstance().markState(
                                    uri, DownloadManager.STATE_PERMANENT_FAILURE);
                        } else {
                            // Mark the failed message as unread.
                            ContentValues readValues = new ContentValues(1);
                            readValues.put(Mms.READ, 0);
                            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                    uri, readValues, null, null);
                            markMmsFailed(mContext);
                        }
                    }

                    values.put(PendingMessages.ERROR_TYPE,  errorType);
                    values.put(PendingMessages.RETRY_INDEX, retryIndex);
                    values.put(PendingMessages.LAST_TRY,    current);

                    int columnIndex = cursor.getColumnIndexOrThrow(
                            PendingMessages._ID);
                    long id = cursor.getLong(columnIndex);
                    SqliteWrapper.update(mContext, mContentResolver,
                            PendingMessages.CONTENT_URI,
                            values, PendingMessages._ID + "=" + id, null);
                } else if (LOCAL_LOGV) {
                    Log.v(TAG, "Cannot found correct pending status for: " + msgId);
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void markMmsFailed(final Context context) {
        Cursor query = context.getContentResolver().query(Mms.CONTENT_URI, new String[]{Mms._ID}, null, null, "date desc");

        try {
            query.moveToFirst();
            String id = query.getString(query.getColumnIndex(Mms._ID));
            query.close();

            // mark message as failed
            ContentValues values = new ContentValues();
            values.put(Mms.MESSAGE_BOX, Mms.MESSAGE_BOX_FAILED);
            String where = Mms._ID + " = '" + id + "'";
            context.getContentResolver().update(Mms.CONTENT_URI, values, where, null);

            BroadcastUtils.sendExplicitBroadcast(
                    mContext, new Intent(), com.klinker.android.send_message.Transaction.REFRESH);
            BroadcastUtils.sendExplicitBroadcast(
                    mContext,
                    new Intent(),
                    com.klinker.android.send_message.Transaction.NOTIFY_SMS_FAILURE);

            // broadcast that mms has failed and you can notify user from there if you would like
            BroadcastUtils.sendExplicitBroadcast(
                    mContext, new Intent(), com.klinker.android.send_message.Transaction.MMS_ERROR);
        } catch (Exception e) {
        }
    }

    private int getResponseStatus(long msgID) {
        int respStatus = 0;
        Cursor cursor = SqliteWrapper.query(mContext, mContentResolver,
                Mms.Outbox.CONTENT_URI, null, Mms._ID + "=" + msgID, null, null);
        try {
            if (cursor.moveToFirst()) {
                respStatus = cursor.getInt(cursor.getColumnIndexOrThrow(Mms.RESPONSE_STATUS));
            }
        } finally {
            cursor.close();
        }
        if (respStatus != 0) {
            Log.e(TAG, "Response status is: " + respStatus);
        }
        return respStatus;
    }

    // apply R880 IOT issue (Conformance 11.6 Retrieve Invalid Message)
    private int getRetrieveStatus(long msgID) {
        int retrieveStatus = 0;
        Cursor cursor = SqliteWrapper.query(mContext, mContentResolver,
                Mms.Inbox.CONTENT_URI, null, Mms._ID + "=" + msgID, null, null);
        try {
            if (cursor.moveToFirst()) {
                retrieveStatus = cursor.getInt(cursor.getColumnIndexOrThrow(
                            Mms.RESPONSE_STATUS));
            }
        } finally {
            cursor.close();
        }
        if (retrieveStatus != 0) {
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "Retrieve status is: " + retrieveStatus);
            }
        }
        return retrieveStatus;
    }

    public static void setRetryAlarm(Context context) {
        Cursor cursor = PduPersister.getPduPersister(context).getPendingMessages(
                Long.MAX_VALUE);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // The result of getPendingMessages() is order by due time.
                    long retryAt = cursor.getLong(cursor.getColumnIndexOrThrow(
                            PendingMessages.DUE_TIME));

                    Intent service = new Intent(TransactionService.ACTION_ONALARM,
                                        null, context, TransactionService.class);
                    PendingIntent operation = PendingIntent.getService(
                            context, 0, service, PendingIntent.FLAG_ONE_SHOT);
                    AlarmManager am = (AlarmManager) context.getSystemService(
                            Context.ALARM_SERVICE);
                    am.set(AlarmManager.RTC, retryAt, operation);

                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "Next retry is scheduled at"
                                + (retryAt - System.currentTimeMillis()) + "ms from now");
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }
}
