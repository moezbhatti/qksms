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

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.provider.Telephony.Threads;
import android.telephony.TelephonyManager;
import com.android.mms.MmsConfig;
import com.android.mms.logs.LogTag;
import com.android.mms.util.DownloadManager;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.NotifyRespInd;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.klinker.android.logger.Log;
import com.klinker.android.send_message.BroadcastUtils;

import java.io.IOException;

import static com.android.mms.transaction.TransactionState.FAILED;
import static com.android.mms.transaction.TransactionState.INITIALIZED;
import static com.android.mms.transaction.TransactionState.SUCCESS;
import static com.google.android.mms.pdu_alt.PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF;
import static com.google.android.mms.pdu_alt.PduHeaders.STATUS_DEFERRED;
import static com.google.android.mms.pdu_alt.PduHeaders.STATUS_RETRIEVED;
import static com.google.android.mms.pdu_alt.PduHeaders.STATUS_UNRECOGNIZED;

/**
 * The NotificationTransaction is responsible for handling multimedia
 * message notifications (M-Notification.ind).  It:
 *
 * <ul>
 * <li>Composes the notification response (M-NotifyResp.ind).
 * <li>Sends the notification response to the MMSC server.
 * <li>Stores the notification indication.
 * <li>Notifies the TransactionService about succesful completion.
 * </ul>
 *
 * NOTE: This MMS client handles all notifications with a <b>deferred
 * retrieval</b> response.  The transaction service, upon succesful
 * completion of this transaction, will trigger a retrieve transaction
 * in case the client is in immediate retrieve mode.
 */
public class NotificationTransaction extends Transaction implements Runnable {
    private static final String TAG = LogTag.TAG;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private Uri mUri;
    private NotificationInd mNotificationInd;
    private String mContentLocation;

    public NotificationTransaction(
            Context context, int serviceId,
            TransactionSettings connectionSettings, String uriString) {
        super(context, serviceId, connectionSettings);

        mUri = Uri.parse(uriString);

        try {
            mNotificationInd = (NotificationInd)
                    PduPersister.getPduPersister(context).load(mUri);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to load NotificationInd from: " + uriString, e);
            throw new IllegalArgumentException();
        }

        mContentLocation = new String(mNotificationInd.getContentLocation());
        mId = mContentLocation;

        // Attach the transaction to the instance of RetryScheduler.
        attach(RetryScheduler.getInstance(context));
    }

    /**
     * This constructor is only used for test purposes.
     */
    public NotificationTransaction(
            Context context, int serviceId,
            TransactionSettings connectionSettings, NotificationInd ind) {
        super(context, serviceId, connectionSettings);

        try {
            // Save the pdu. If we can start downloading the real pdu immediately, don't allow
            // persist() to create a thread for the notificationInd because it causes UI jank.
            mUri = PduPersister.getPduPersister(context).persist(
                        ind, Inbox.CONTENT_URI, !allowAutoDownload(mContext),
                    true, null);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to save NotificationInd in constructor.", e);
            throw new IllegalArgumentException();
        }

        mNotificationInd = ind;
        mId = new String(mNotificationInd.getContentLocation());
    }

    /*
     * (non-Javadoc)
     * @see com.google.android.mms.pdu.Transaction#process()
     */
    @Override
    public void process() {
        new Thread(this, "NotificationTransaction").start();
    }

    public static boolean allowAutoDownload(Context context) {
        try { Looper.prepare(); } catch (Exception e) { }
        boolean autoDownload = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("auto_download_mms", true);
        boolean dataSuspended = (((TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE)).getDataState() ==
                TelephonyManager.DATA_SUSPENDED);
        return autoDownload && !dataSuspended;
    }

    public void run() {
        try { Looper.prepare(); } catch (Exception e) {}
        DownloadManager.init(mContext);
        DownloadManager downloadManager = DownloadManager.getInstance();
        boolean autoDownload = allowAutoDownload(mContext);
        try {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Notification transaction launched: " + this);
            }

            // By default, we set status to STATUS_DEFERRED because we
            // should response MMSC with STATUS_DEFERRED when we cannot
            // download a MM immediately.
            int status = STATUS_DEFERRED;
            // Don't try to download when data is suspended, as it will fail, so defer download
            if (!autoDownload) {
                downloadManager.markState(mUri, DownloadManager.STATE_UNSTARTED);
                sendNotifyRespInd(status);
                return;
            }

            downloadManager.markState(mUri, DownloadManager.STATE_DOWNLOADING);

            if (LOCAL_LOGV) {
                Log.v(TAG, "Content-Location: " + mContentLocation);
            }

            byte[] retrieveConfData = null;
            // We should catch exceptions here to response MMSC
            // with STATUS_DEFERRED.
            try {
                retrieveConfData = getPdu(mContentLocation);
            } catch (IOException e) {
                mTransactionState.setState(FAILED);
            }

            if (retrieveConfData != null) {
                GenericPdu pdu = new PduParser(retrieveConfData).parse();
                if ((pdu == null) || (pdu.getMessageType() != MESSAGE_TYPE_RETRIEVE_CONF)) {
                    Log.e(TAG, "Invalid M-RETRIEVE.CONF PDU. " +
                            (pdu != null ? "message type: " + pdu.getMessageType() : "null pdu"));
                    mTransactionState.setState(FAILED);
                    status = STATUS_UNRECOGNIZED;
                } else {
                    // Save the received PDU (must be a M-RETRIEVE.CONF).
                    PduPersister p = PduPersister.getPduPersister(mContext);
                    Uri uri = p.persist(pdu, Inbox.CONTENT_URI, true,
                            true, null);

                    // Use local time instead of PDU time
                    ContentValues values = new ContentValues(1);
                    values.put(Mms.DATE, System.currentTimeMillis() / 1000L);
                    SqliteWrapper.update(mContext, mContext.getContentResolver(),
                            uri, values, null, null);

                    // We have successfully downloaded the new MM. Delete the
                    // M-NotifyResp.ind from Inbox.
                    SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                                         mUri, null, null);
                    Log.v(TAG, "NotificationTransaction received new mms message: " + uri);
                    // Delete obsolete threads
                    SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                            Threads.OBSOLETE_THREADS_URI, null, null);

                    // Notify observers with newly received MM.
                    mUri = uri;
                    status = STATUS_RETRIEVED;
                    BroadcastUtils.sendExplicitBroadcast(
                            mContext,
                            new Intent(),
                            com.klinker.android.send_message.Transaction.NOTIFY_OF_MMS);
                }
            }

            if (LOCAL_LOGV) {
                Log.v(TAG, "status=0x" + Integer.toHexString(status));
            }

            // Check the status and update the result state of this Transaction.
            switch (status) {
                case STATUS_RETRIEVED:
                    mTransactionState.setState(SUCCESS);
                    break;
                case STATUS_DEFERRED:
                    // STATUS_DEFERRED, may be a failed immediate retrieval.
                    if (mTransactionState.getState() == INITIALIZED) {
                        mTransactionState.setState(SUCCESS);
                    }
                    break;
            }

            sendNotifyRespInd(status);
        } catch (Throwable t) {
            Log.e(TAG, "error", t);
        } finally {
            mTransactionState.setContentUri(mUri);
            if (!autoDownload) {
                // Always mark the transaction successful for deferred
                // download since any error here doesn't make sense.
                mTransactionState.setState(SUCCESS);
            }
            if (mTransactionState.getState() != SUCCESS) {
                mTransactionState.setState(FAILED);
                Log.e(TAG, "NotificationTransaction failed.");
            }
            notifyObservers();
        }
    }

    private void sendNotifyRespInd(int status) throws MmsException, IOException {
        // Create the M-NotifyResp.ind
        NotifyRespInd notifyRespInd = new NotifyRespInd(
                PduHeaders.CURRENT_MMS_VERSION,
                mNotificationInd.getTransactionId(),
                status);

        // Pack M-NotifyResp.ind and send it
        if(MmsConfig.getNotifyWapMMSC()) {
            sendPdu(new PduComposer(mContext, notifyRespInd).make(), mContentLocation);
        } else {
            sendPdu(new PduComposer(mContext, notifyRespInd).make());
        }
    }

    @Override
    public int getType() {
        return NOTIFICATION_TRANSACTION;
    }
}
