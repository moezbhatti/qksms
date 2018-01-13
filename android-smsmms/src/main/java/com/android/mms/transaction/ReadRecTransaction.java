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

import java.io.IOException;

import android.content.Context;
import android.net.Uri;
import android.provider.Telephony.Mms.Sent;
import com.klinker.android.logger.Log;

import com.android.mms.logs.LogTag;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.ReadRecInd;
import com.klinker.android.send_message.Utils;

/**
 * The ReadRecTransaction is responsible for sending read report
 * notifications (M-read-rec.ind) to clients that have requested them.
 * It:
 *
 * <ul>
 * <li>Loads the read report indication from storage (Outbox).
 * <li>Packs M-read-rec.ind and sends it.
 * <li>Notifies the TransactionService about succesful completion.
 * </ul>
 */
public class ReadRecTransaction extends Transaction implements Runnable{
    private static final String TAG = LogTag.TAG;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private Thread mThread;
    private final Uri mReadReportURI;

    public ReadRecTransaction(Context context,
            int transId,
            TransactionSettings connectionSettings,
            String uri) {
        super(context, transId, connectionSettings);
        mReadReportURI = Uri.parse(uri);
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
        mThread = new Thread(this, "ReadRecTransaction");
        mThread.start();
    }

    public void run() {
        PduPersister persister = PduPersister.getPduPersister(mContext);

        try {
            // Load M-read-rec.ind from outbox
            ReadRecInd readRecInd = (ReadRecInd) persister.load(mReadReportURI);

            // insert the 'from' address per spec
            String lineNumber = Utils.getMyPhoneNumber(mContext);
            readRecInd.setFrom(new EncodedStringValue(lineNumber));

            // Pack M-read-rec.ind and send it
            byte[] postingData = new PduComposer(mContext, readRecInd).make();
            sendPdu(postingData);

            Uri uri = persister.move(mReadReportURI, Sent.CONTENT_URI);
            mTransactionState.setState(TransactionState.SUCCESS);
            mTransactionState.setContentUri(uri);
        } catch (IOException e) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Failed to send M-Read-Rec.Ind.", e);
            }
        } catch (MmsException e) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Failed to load message from Outbox.", e);
            }
        } catch (RuntimeException e) {
            if (LOCAL_LOGV) {
                Log.e(TAG, "Unexpected RuntimeException.", e);
            }
        } finally {
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mReadReportURI);
            }
            notifyObservers();
        }
    }

    @Override
    public int getType() {
        return READREC_TRANSACTION;
    }
}
