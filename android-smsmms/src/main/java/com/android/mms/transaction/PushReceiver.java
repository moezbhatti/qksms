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

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;

import com.android.mms.logs.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.service_alt.DownloadRequest;
import com.android.mms.service_alt.MmsNetworkManager;
import com.android.mms.service_alt.MmsRequestManager;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.DeliveryInd;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.ReadOrigInd;
import com.klinker.android.logger.Log;
import com.klinker.android.send_message.BroadcastUtils;
import com.klinker.android.send_message.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.provider.Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION;
import static android.provider.Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION;
import static com.google.android.mms.pdu_alt.PduHeaders.MESSAGE_TYPE_DELIVERY_IND;
import static com.google.android.mms.pdu_alt.PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
import static com.google.android.mms.pdu_alt.PduHeaders.MESSAGE_TYPE_READ_ORIG_IND;

/**
 * Receives Intent.WAP_PUSH_RECEIVED_ACTION intents and starts the
 * TransactionService by passing the push-data to it.
 */
public class PushReceiver extends BroadcastReceiver {
    private static final String TAG = LogTag.TAG;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = true;

    static final String[] PROJECTION = new String[] {
            Mms.CONTENT_LOCATION,
            Mms.LOCKED
    };

    static final int COLUMN_CONTENT_LOCATION      = 0;

    private static Set<String> downloadedUrls = new HashSet<String>();
    private static final ExecutorService PUSH_RECEIVER_EXECUTOR = Executors.newSingleThreadExecutor();

    private class ReceivePushTask extends AsyncTask<Intent,Void,Void> {
        private Context mContext;
        public ReceivePushTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Intent... intents) {
            Log.v(TAG, "receiving a new mms message");
            Intent intent = intents[0];

            // Get raw PDU push-data from the message and parse it
            byte[] pushData = intent.getByteArrayExtra("data");
            PduParser parser = new PduParser(pushData);
            GenericPdu pdu = parser.parse();

            if (null == pdu) {
                Log.e(TAG, "Invalid PUSH data");
                return null;
            }

            PduPersister p = PduPersister.getPduPersister(mContext);
            ContentResolver cr = mContext.getContentResolver();
            int type = pdu.getMessageType();
            long threadId = -1;

            try {
                switch (type) {
                    case MESSAGE_TYPE_DELIVERY_IND:
                    case MESSAGE_TYPE_READ_ORIG_IND: {
                        threadId = findThreadId(mContext, pdu, type);
                        if (threadId == -1) {
                            // The associated SendReq isn't found, therefore skip
                            // processing this PDU.
                            break;
                        }

                        boolean group;

                        try {
                            group = com.klinker.android.send_message.Transaction.settings.getGroup();
                        } catch (Exception e) {
                            group = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("group_message", true);
                        }

                        Uri uri = p.persist(pdu, Uri.parse("content://mms/inbox"), true,
                                group, null);
                        // Update thread ID for ReadOrigInd & DeliveryInd.
                        ContentValues values = new ContentValues(1);
                        values.put(Mms.THREAD_ID, threadId);
                        SqliteWrapper.update(mContext, cr, uri, values, null, null);
                        break;
                    }
                    case MESSAGE_TYPE_NOTIFICATION_IND: {
                        NotificationInd nInd = (NotificationInd) pdu;

                        if (MmsConfig.getTransIdEnabled()) {
                            byte [] contentLocation = nInd.getContentLocation();
                            if ('=' == contentLocation[contentLocation.length - 1]) {
                                byte [] transactionId = nInd.getTransactionId();
                                byte [] contentLocationWithId = new byte [contentLocation.length
                                                                          + transactionId.length];
                                System.arraycopy(contentLocation, 0, contentLocationWithId,
                                        0, contentLocation.length);
                                System.arraycopy(transactionId, 0, contentLocationWithId,
                                        contentLocation.length, transactionId.length);
                                nInd.setContentLocation(contentLocationWithId);
                            }
                        }

                        if (!isDuplicateNotification(mContext, nInd)) {
                            // Save the pdu. If we can start downloading the real pdu immediately,
                            // don't allow persist() to create a thread for the notificationInd
                            // because it causes UI jank.
                            boolean group;

                            try {
                                group = com.klinker.android.send_message.Transaction.settings.getGroup();
                            } catch (Exception e) {
                                group = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("group_message", true);
                            }

                            Uri uri = p.persist(pdu, Inbox.CONTENT_URI,
                                    !NotificationTransaction.allowAutoDownload(mContext),
                                    group,
                                    null);

                            String location = getContentLocation(mContext, uri);
                            if (downloadedUrls.contains(location)) {
                                Log.v(TAG, "already added this download, don't download again");
                                return null;
                            } else {
                                downloadedUrls.add(location);
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                Log.v(TAG, "receiving on a lollipop+ device");
                                boolean useSystem = true;

                                if (com.klinker.android.send_message.Transaction.settings != null) {
                                    useSystem = com.klinker.android.send_message.Transaction.settings
                                            .getUseSystemSending();
                                } else {
                                    useSystem = PreferenceManager.getDefaultSharedPreferences(mContext)
                                            .getBoolean("system_mms_sending", useSystem);
                                }

                                if (useSystem) {
                                    DownloadManager.getInstance().downloadMultimediaMessage(mContext, location, uri, true);
                                } else {
                                    Log.v(TAG, "receiving with lollipop method");
                                    MmsRequestManager requestManager = new MmsRequestManager(mContext);
                                    DownloadRequest request = new DownloadRequest(requestManager,
                                            Utils.getDefaultSubscriptionId(),
                                            location, uri, null, null,
                                            null, mContext);
                                    MmsNetworkManager manager = new MmsNetworkManager(mContext, Utils.getDefaultSubscriptionId());
                                    request.execute(mContext, manager);
                                }
                            } else {
                                if (NotificationTransaction.allowAutoDownload(mContext)) {
                                    // Start service to finish the notification transaction.
                                    Intent svc = new Intent(mContext, TransactionService.class);
                                    svc.putExtra(TransactionBundle.URI, uri.toString());
                                    svc.putExtra(TransactionBundle.TRANSACTION_TYPE,
                                            Transaction.NOTIFICATION_TRANSACTION);
                                    svc.putExtra(TransactionBundle.LOLLIPOP_RECEIVING,
                                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
                                    mContext.startService(svc);
                                } else {
                                    Intent notificationBroadcast = new Intent(com.klinker.android.send_message.Transaction.NOTIFY_OF_MMS);
                                    notificationBroadcast.putExtra("receive_through_stock", true);
                                    BroadcastUtils.sendExplicitBroadcast(
                                            mContext,
                                            notificationBroadcast,
                                            com.klinker.android.send_message.Transaction.NOTIFY_OF_MMS);
                                }
                            }
                        } else if (LOCAL_LOGV) {
                            Log.v(TAG, "Skip downloading duplicate message: "
                                    + new String(nInd.getContentLocation()));
                        }
                        break;
                    }
                    default:
                        Log.e(TAG, "Received unrecognized PDU.");
                }
            } catch (MmsException e) {
                Log.e(TAG, "Failed to save the data from PUSH: type=" + type, e);
            } catch (RuntimeException e) {
                Log.e(TAG, "Unexpected RuntimeException.", e);
            }

            if (LOCAL_LOGV) {
                Log.v(TAG, "PUSH Intent processed.");
            }

            return null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, intent.getAction() + " " + intent.getType());
        if ((intent.getAction().equals(WAP_PUSH_DELIVER_ACTION) || intent.getAction().equals(WAP_PUSH_RECEIVED_ACTION))
                && ContentType.MMS_MESSAGE.equals(intent.getType())) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Received PUSH Intent: " + intent);
            }

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            if ((!sharedPrefs.getBoolean("receive_with_stock", false) && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && sharedPrefs.getBoolean("override", true))
                    || Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Hold a wake lock for 5 seconds, enough to give any
                // services we start time to take their own wake locks.
                PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "MMS PushReceiver");
                wl.acquire(5000);
                MmsConfig.init(context);
                new ReceivePushTask(context).executeOnExecutor(PUSH_RECEIVER_EXECUTOR, intent);

                Log.v("mms_receiver", context.getPackageName() + " received and aborted");

                abortBroadcast();
            } else {
                clearAbortBroadcast();
                Intent notificationBroadcast = new Intent(com.klinker.android.send_message.Transaction.NOTIFY_OF_MMS);
                notificationBroadcast.putExtra("receive_through_stock", true);
                BroadcastUtils.sendExplicitBroadcast(
                        context,
                        notificationBroadcast,
                        com.klinker.android.send_message.Transaction.NOTIFY_OF_MMS);

                Log.v("mms_receiver", context.getPackageName() + " received and not aborted");
            }
        }
    }

    public static String getContentLocation(Context context, Uri uri)
            throws MmsException {
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                uri, PROJECTION, null, null, null);

        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    String location = cursor.getString(COLUMN_CONTENT_LOCATION);
                    cursor.close();
                    return location;
                }
            } finally {
                cursor.close();
            }
        }

        throw new MmsException("Cannot get X-Mms-Content-Location from: " + uri);
    }

    private static long findThreadId(Context context, GenericPdu pdu, int type) {
        String messageId;

        if (type == MESSAGE_TYPE_DELIVERY_IND) {
            messageId = new String(((DeliveryInd) pdu).getMessageId());
        } else {
            messageId = new String(((ReadOrigInd) pdu).getMessageId());
        }

        StringBuilder sb = new StringBuilder('(');
        sb.append(Mms.MESSAGE_ID);
        sb.append('=');
        sb.append(DatabaseUtils.sqlEscapeString(messageId));
        sb.append(" AND ");
        sb.append(Mms.MESSAGE_TYPE);
        sb.append('=');
        sb.append(PduHeaders.MESSAGE_TYPE_SEND_REQ);
        // TODO ContentResolver.query() appends closing ')' to the selection argument
        // sb.append(')');

        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                            Mms.CONTENT_URI, new String[] { Mms.THREAD_ID },
                            sb.toString(), null, null);
        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    long id = cursor.getLong(0);
                    cursor.close();
                    return id;
                }
            } finally {
                cursor.close();
            }
        }

        return -1;
    }

    private static boolean isDuplicateNotification(
            Context context, NotificationInd nInd) {
        byte[] rawLocation = nInd.getContentLocation();
        if (rawLocation != null) {
            String location = new String(rawLocation);
            String selection = Mms.CONTENT_LOCATION + " = ?";
            String[] selectionArgs = new String[] { location };
            Cursor cursor = SqliteWrapper.query(
                    context, context.getContentResolver(),
                    Mms.CONTENT_URI, new String[] { Mms._ID },
                    selection, selectionArgs, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        // We already received the same notification before.
                        cursor.close();
                        //return true;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }
}
