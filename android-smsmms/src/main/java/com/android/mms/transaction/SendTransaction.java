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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Sent;
import android.text.TextUtils;

import com.android.mms.logs.LogTag;
import com.android.mms.util.RateController;
import com.android.mms.util.SendingProgressTokenManager;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.SendConf;
import com.google.android.mms.pdu_alt.SendReq;
import com.klinker.android.logger.Log;
import com.klinker.android.send_message.BroadcastUtils;
import com.klinker.android.send_message.Utils;

import java.util.Arrays;

/**
 * The SendTransaction is responsible for sending multimedia messages
 * (M-Send.req) to the MMSC server.  It:
 *
 * <ul>
 * <li>Loads the multimedia message from storage (Outbox).
 * <li>Packs M-Send.req and sends it.
 * <li>Retrieves confirmation data from the server  (M-Send.conf).
 * <li>Parses confirmation message and handles it.
 * <li>Moves sent multimedia message from Outbox to Sent.
 * <li>Notifies the TransactionService about successful completion.
 * </ul>
 */
public class SendTransaction extends Transaction implements Runnable {
    private static final String TAG = LogTag.TAG;

    private Thread mThread;
    public final Uri mSendReqURI;

    public SendTransaction(Context context,
            int transId, TransactionSettings connectionSettings, String uri) {
        super(context, transId, connectionSettings);
        mSendReqURI = Uri.parse(uri);
        mId = uri;

        // Attach the transaction to the instance of RetryScheduler.
        attach(RetryScheduler.getInstance(context));
    }

    /*
     * (non-Javadoc)
     * @see com.android.mms.Transaction#process()
     */
    @Override
    public void process() {
        mThread = new Thread(this, "SendTransaction");
        mThread.start();
    }

    public void run() {
        StringBuilder builder = new StringBuilder();
        try {
            RateController.init(mContext);
            RateController rateCtlr = RateController.getInstance();
            if (rateCtlr.isLimitSurpassed() && !rateCtlr.isAllowedByUser()) {
                Log.e(TAG, "Sending rate limit surpassed.");
                return;
            }

            // Load M-Send.req from outbox
            PduPersister persister = PduPersister.getPduPersister(mContext);
            SendReq sendReq = (SendReq) persister.load(mSendReqURI);

            // Update the 'date' field of the PDU right before sending it.
            long date = System.currentTimeMillis() / 1000L;
            sendReq.setDate(date);

            // Persist the new date value into database.
            ContentValues values = new ContentValues(1);
            values.put(Mms.DATE, date);
            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                 mSendReqURI, values, null, null);

            // fix bug 2100169: insert the 'from' address per spec
            String lineNumber = Utils.getMyPhoneNumber(mContext);
            if (!TextUtils.isEmpty(lineNumber)) {
                sendReq.setFrom(new EncodedStringValue(lineNumber));
            }

            // Pack M-Send.req, send it, retrieve confirmation data, and parse it
            long tokenKey = ContentUris.parseId(mSendReqURI);
            byte[] response = sendPdu(SendingProgressTokenManager.get(tokenKey),
                                      new PduComposer(mContext, sendReq).make());
            SendingProgressTokenManager.remove(tokenKey);

            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                String respStr = new String(response);
                builder.append("[SendTransaction] run: send mms msg (" + mId + "), resp=" + respStr);
                Log.d(TAG, "[SendTransaction] run: send mms msg (" + mId + "), resp=" + respStr);
            }

            SendConf conf = (SendConf) new PduParser(response).parse();
            if (conf == null) {
                Log.e(TAG, "No M-Send.conf received.");
                builder.append("No M-Send.conf received.\n");
            }

            // Check whether the responding Transaction-ID is consistent
            // with the sent one.
            byte[] reqId = sendReq.getTransactionId();
            byte[] confId = conf.getTransactionId();
            if (!Arrays.equals(reqId, confId)) {
                Log.e(TAG, "Inconsistent Transaction-ID: req="
                        + new String(reqId) + ", conf=" + new String(confId));
                builder.append("Inconsistent Transaction-ID: req="
                        + new String(reqId) + ", conf=" + new String(confId) + "\n");
                return;
            }

            // From now on, we won't save the whole M-Send.conf into
            // our database. Instead, we just save some interesting fields
            // into the related M-Send.req.
            values = new ContentValues(2);
            int respStatus = conf.getResponseStatus();
            values.put(Mms.RESPONSE_STATUS, respStatus);

            if (respStatus != PduHeaders.RESPONSE_STATUS_OK) {
                SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                     mSendReqURI, values, null, null);
                Log.e(TAG, "Server returned an error code: " + respStatus);
                builder.append("Server returned an error code: " + respStatus + "\n");
                return;
            }

            String messageId = PduPersister.toIsoString(conf.getMessageId());
            values.put(Mms.MESSAGE_ID, messageId);
            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                 mSendReqURI, values, null, null);

            // Move M-Send.req from Outbox into Sent.
            Uri uri = persister.move(mSendReqURI, Sent.CONTENT_URI);

            mTransactionState.setState(TransactionState.SUCCESS);
            mTransactionState.setContentUri(uri);
        } catch (Throwable t) {
            Log.e(TAG, "error", t);
        } finally {
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mSendReqURI);
                Log.e(TAG, "Delivery failed.");
                builder.append("Delivery failed\n");

                Intent intent = new Intent(com.klinker.android.send_message.Transaction.MMS_ERROR);
                intent.putExtra("stack", builder.toString());
                BroadcastUtils.sendExplicitBroadcast(
                        mContext, intent, com.klinker.android.send_message.Transaction.MMS_ERROR);
            }
            notifyObservers();
        }
    }

    @Override
    public int getType() {
        return SEND_TRANSACTION;
    }
}
