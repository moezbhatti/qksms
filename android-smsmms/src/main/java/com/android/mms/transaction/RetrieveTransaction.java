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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.android.mms.logs.LogTag;
import com.android.mms.util.DownloadManager;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.AcknowledgeInd;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.RetrieveConf;
import com.klinker.android.logger.Log;
import com.klinker.android.send_message.Utils;

import java.io.IOException;

/**
 * The RetrieveTransaction is responsible for retrieving multimedia
 * messages (M-Retrieve.conf) from the MMSC server.  It:
 *
 * <ul>
 * <li>Sends a GET request to the MMSC server.
 * <li>Retrieves the binary M-Retrieve.conf data and parses it.
 * <li>Persists the retrieve multimedia message.
 * <li>Determines whether an acknowledgement is required.
 * <li>Creates appropriate M-Acknowledge.ind and sends it to MMSC server.
 * <li>Notifies the TransactionService about succesful completion.
 * </ul>
 */
public class RetrieveTransaction extends Transaction implements Runnable {
    private static final String TAG = LogTag.TAG;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private final Uri mUri;
    private final String mContentLocation;
    private boolean mLocked;

    static final String[] PROJECTION = new String[] {
        Mms.CONTENT_LOCATION,
        Mms.LOCKED
    };

    // The indexes of the columns which must be consistent with above PROJECTION.
    static final int COLUMN_CONTENT_LOCATION      = 0;
    static final int COLUMN_LOCKED                = 1;

    public RetrieveTransaction(Context context, int serviceId,
            TransactionSettings connectionSettings, String uri)
            throws MmsException {
        super(context, serviceId, connectionSettings);

        if (uri.startsWith("content://")) {
            mUri = Uri.parse(uri); // The Uri of the M-Notification.ind
            mId = mContentLocation = getContentLocation(context, mUri);
            if (LOCAL_LOGV) {
                Log.v(TAG, "X-Mms-Content-Location: " + mContentLocation);
            }
        } else {
            throw new IllegalArgumentException(
                    "Initializing from X-Mms-Content-Location is abandoned!");
        }

        // Attach the transaction to the instance of RetryScheduler.
        attach(RetryScheduler.getInstance(context));
    }

    public String getContentLocation(Context context, Uri uri)
            throws MmsException {
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                            uri, PROJECTION, null, null, null);
        mLocked = false;

        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    // Get the locked flag from the M-Notification.ind so it can be transferred
                    // to the real message after the download.
                    mLocked = cursor.getInt(COLUMN_LOCKED) == 1;
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

    /*
     * (non-Javadoc)
     * @see com.android.mms.transaction.Transaction#process()
     */
    @Override
    public void process() {
        new Thread(this, "RetrieveTransaction").start();
    }

    public void run() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            DownloadRequest request = new DownloadRequest(mContentLocation, mUri, null, null, null);
//            MmsNetworkManager manager = new MmsNetworkManager(mContext);
//            request.execute(mContext, manager);
//        } else {
            try {
                // Change the downloading state of the M-Notification.ind.
                DownloadManager.init(mContext.getApplicationContext());
                DownloadManager.getInstance().markState(
                        mUri, DownloadManager.STATE_DOWNLOADING);

                // Send GET request to MMSC and retrieve the response data.
                byte[] resp = getPdu(mContentLocation);

                // Parse M-Retrieve.conf
                RetrieveConf retrieveConf = (RetrieveConf) new PduParser(resp).parse();
                if (null == retrieveConf) {
                    throw new MmsException("Invalid M-Retrieve.conf PDU.");
                }

                Uri msgUri = null;
                if (isDuplicateMessage(mContext, retrieveConf)) {
                    // Mark this transaction as failed to prevent duplicate
                    // notification to user.
                    mTransactionState.setState(TransactionState.FAILED);
                    mTransactionState.setContentUri(mUri);
                } else {
                    // Store M-Retrieve.conf into Inbox
                    PduPersister persister = PduPersister.getPduPersister(mContext);
                    msgUri = persister.persist(retrieveConf, Inbox.CONTENT_URI, true,
                            true, null);

                    // Use local time instead of PDU time
                    ContentValues values = new ContentValues(2);
                    values.put(Mms.DATE, System.currentTimeMillis() / 1000L);
                    values.put(Mms.MESSAGE_SIZE, resp.length);
                    SqliteWrapper.update(mContext, mContext.getContentResolver(),
                            msgUri, values, null, null);

                    // The M-Retrieve.conf has been successfully downloaded.
                    mTransactionState.setState(TransactionState.SUCCESS);
                    mTransactionState.setContentUri(msgUri);
                    // Remember the location the message was downloaded from.
                    // Since it's not critical, it won't fail the transaction.
                    // Copy over the locked flag from the M-Notification.ind in case
                    // the user locked the message before activating the download.
                    updateContentLocation(mContext, msgUri, mContentLocation, mLocked);
                }

                // Delete the corresponding M-Notification.ind.
                SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                        mUri, null, null);

                // Send ACK to the Proxy-Relay to indicate we have fetched the
                // MM successfully.
                // Don't mark the transaction as failed if we failed to send it.
                sendAcknowledgeInd(retrieveConf);
            } catch (Throwable t) {
                Log.e(TAG, "error", t);
                if ("HTTP error: Not Found".equals(t.getMessage())) {
                    // Delete the expired M-Notification.ind.
                    SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                            mUri, null, null);
                }
            } finally {
                if (mTransactionState.getState() != TransactionState.SUCCESS) {
                    mTransactionState.setState(TransactionState.FAILED);
                    mTransactionState.setContentUri(mUri);
                    Log.e(TAG, "Retrieval failed.");
                }
                notifyObservers();
            }
//        }
    }

    private static boolean isDuplicateMessage(Context context, RetrieveConf rc) {
        byte[] rawMessageId = rc.getMessageId();
        if (rawMessageId != null) {
            String messageId = new String(rawMessageId);
            String selection = "(" + Mms.MESSAGE_ID + " = ? AND "
                                   + Mms.MESSAGE_TYPE + " = ?)";
            String[] selectionArgs = new String[] { messageId,
                    String.valueOf(PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF) };

            Cursor cursor = SqliteWrapper.query(
                    context, context.getContentResolver(),
                    Mms.CONTENT_URI, new String[] { Mms._ID, Mms.SUBJECT, Mms.SUBJECT_CHARSET },
                    selection, selectionArgs, null);

            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        // A message with identical message ID and type found.
                        // Do some additional checks to be sure it's a duplicate.
                        boolean dup = isDuplicateMessageExtra(cursor, rc);
                        if (!cursor.isClosed()) {
                            cursor.close();
                        }

                        return dup;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }

    private static boolean isDuplicateMessageExtra(Cursor cursor, RetrieveConf rc) {
        // Compare message subjects, taking encoding into account
        EncodedStringValue encodedSubjectReceived = null;
        EncodedStringValue encodedSubjectStored = null;
        String subjectReceived = null;
        String subjectStored = null;
        String subject = null;

        encodedSubjectReceived = rc.getSubject();
        if (encodedSubjectReceived != null) {
            subjectReceived = encodedSubjectReceived.getString();
        }

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int subjectIdx = cursor.getColumnIndex(Mms.SUBJECT);
            int charsetIdx = cursor.getColumnIndex(Mms.SUBJECT_CHARSET);
            subject = cursor.getString(subjectIdx);
            int charset = cursor.getInt(charsetIdx);
            if (subject != null) {
                encodedSubjectStored = new EncodedStringValue(charset, PduPersister
                        .getBytes(subject));
            }
            if (encodedSubjectStored == null && encodedSubjectReceived == null) {
                // Both encoded subjects are null - return true
                return true;
            } else if (encodedSubjectStored != null && encodedSubjectReceived != null) {
                subjectStored = encodedSubjectStored.getString();
                if (!TextUtils.isEmpty(subjectStored) && !TextUtils.isEmpty(subjectReceived)) {
                    // Both decoded subjects are non-empty - compare them
                    return subjectStored.equals(subjectReceived);
                } else if (TextUtils.isEmpty(subjectStored) && TextUtils.isEmpty(subjectReceived)) {
                    // Both decoded subjects are "" - return true
                    return true;
                }
            }
        }

        return false;
    }

    private void sendAcknowledgeInd(RetrieveConf rc) throws MmsException, IOException {
        // Send M-Acknowledge.ind to MMSC if required.
        // If the Transaction-ID isn't set in the M-Retrieve.conf, it means
        // the MMS proxy-relay doesn't require an ACK.
        byte[] tranId = rc.getTransactionId();
        if (tranId != null) {
            // Create M-Acknowledge.ind
            AcknowledgeInd acknowledgeInd = new AcknowledgeInd(
                    PduHeaders.CURRENT_MMS_VERSION, tranId);

            // insert the 'from' address per spec
            String lineNumber = Utils.getMyPhoneNumber(mContext);
            acknowledgeInd.setFrom(new EncodedStringValue(lineNumber));

            // Pack M-Acknowledge.ind and send it
            if(MmsConfig.getNotifyWapMMSC()) {
                sendPdu(new PduComposer(mContext, acknowledgeInd).make(), mContentLocation);
            } else {
                sendPdu(new PduComposer(mContext, acknowledgeInd).make());
            }
        }
    }

    private static void updateContentLocation(Context context, Uri uri,
                                              String contentLocation,
                                              boolean locked) {
        ContentValues values = new ContentValues(2);
        values.put(Mms.CONTENT_LOCATION, contentLocation);
        values.put(Mms.LOCKED, locked);     // preserve the state of the M-Notification.ind lock.
        SqliteWrapper.update(context, context.getContentResolver(),
                             uri, values, null, null);
    }

    @Override
    public int getType() {
        return RETRIEVE_TRANSACTION;
    }
}
