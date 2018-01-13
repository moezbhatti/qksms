package com.klinker.android.send_message;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsManager;

import com.android.mms.service_alt.DownloadRequest;
import com.android.mms.service_alt.MmsConfig;
import com.android.mms.transaction.DownloadManager;
import com.android.mms.transaction.HttpUtils;
import com.android.mms.transaction.TransactionSettings;
import com.android.mms.util.SendingProgressTokenManager;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.NotifyRespInd;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.RetrieveConf;
import com.google.android.mms.util_alt.SqliteWrapper;
import com.klinker.android.logger.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.google.android.mms.pdu_alt.PduHeaders.STATUS_RETRIEVED;
import static com.klinker.android.send_message.MmsReceivedReceiver.EXTRA_FILE_PATH;
import static com.klinker.android.send_message.MmsReceivedReceiver.EXTRA_LOCATION_URL;
import static com.klinker.android.send_message.MmsReceivedReceiver.EXTRA_TRIGGER_PUSH;
import static com.klinker.android.send_message.MmsReceivedReceiver.EXTRA_URI;

public class MmsReceivedService extends IntentService {
    private static final String TAG = "MmsReceivedService";

    private static final String LOCATION_SELECTION =
            Telephony.Mms.MESSAGE_TYPE + "=? AND " + Telephony.Mms.CONTENT_LOCATION + " =?";

    public MmsReceivedService() {
        super("MmsReceivedService");
    }

    public MmsReceivedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "MMS has finished downloading, persisting it to the database");

        String path = intent.getStringExtra(EXTRA_FILE_PATH);
        Log.v(TAG, path);

        FileInputStream reader = null;
        try {
            File mDownloadFile = new File(path);
            final int nBytes = (int) mDownloadFile.length();
            reader = new FileInputStream(mDownloadFile);
            final byte[] response = new byte[nBytes];
            reader.read(response, 0, nBytes);

            CommonNotificationTask task = getNotificationTask(this, intent, response);
            executeNotificationTask(task);

            DownloadRequest.persist(this, response,
                    new MmsConfig.Overridden(new MmsConfig(this), null),
                    intent.getStringExtra(EXTRA_LOCATION_URL),
                    Utils.getDefaultSubscriptionId(), null);

            Log.v(TAG, "response saved successfully");
            Log.v(TAG, "response length: " + response.length);
            mDownloadFile.delete();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "MMS received, file not found exception", e);
        } catch (IOException e) {
            Log.e(TAG, "MMS received, io exception", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "MMS received, io exception", e);
                }
            }

            handleHttpError(this, intent);
            DownloadManager.finishDownload(intent.getStringExtra(EXTRA_LOCATION_URL));
        }
    }

    private static void handleHttpError(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        
        final int httpError = intent.getIntExtra(SmsManager.EXTRA_MMS_HTTP_STATUS, 0);
        if (httpError == 404 ||
                httpError == 400) {
            // Delete the corresponding NotificationInd
            SqliteWrapper.delete(context,
                    context.getContentResolver(),
                    Telephony.Mms.CONTENT_URI,
                    LOCATION_SELECTION,
                    new String[]{
                            Integer.toString(PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND),
                            intent.getStringExtra(EXTRA_LOCATION_URL)
                    });
        }
    }

    private static NotificationInd getNotificationInd(Context context, Intent intent) throws MmsException {
        return (NotificationInd) PduPersister.getPduPersister(context).load((Uri) intent.getParcelableExtra(EXTRA_URI));
    }

    private static abstract class CommonNotificationTask {
        protected final Context mContext;
        private final TransactionSettings mTransactionSettings;
        final NotificationInd mNotificationInd;
        final String mContentLocation;

        CommonNotificationTask(Context context, TransactionSettings settings, NotificationInd ind) {
            mContext = context;
            mTransactionSettings = settings;
            mNotificationInd = ind;
            mContentLocation = new String(ind.getContentLocation());
        }

        /**
         * A common method to send a PDU to MMSC.
         *
         * @param pdu A byte array which contains the data of the PDU.
         * @param mmscUrl Url of the recipient MMSC.
         * @return A byte array which contains the response data.
         *         If an HTTP error code is returned, an IOException will be thrown.
         * @throws java.io.IOException if any error occurred on network interface or
         *         an HTTP error code(>=400) returned from the server.
         * @throws com.google.android.mms.MmsException if pdu is null.
         */
        byte[] sendPdu(byte[] pdu, String mmscUrl) throws IOException, MmsException {
            return sendPdu(SendingProgressTokenManager.NO_TOKEN, pdu, mmscUrl);
        }

        /**
         * A common method to send a PDU to MMSC.
         *
         * @param pdu A byte array which contains the data of the PDU.
         * @return A byte array which contains the response data.
         *         If an HTTP error code is returned, an IOException will be thrown.
         * @throws java.io.IOException if any error occurred on network interface or
         *         an HTTP error code(>=400) returned from the server.
         * @throws com.google.android.mms.MmsException if pdu is null.
         */
        byte[] sendPdu(byte[] pdu) throws IOException, MmsException {
            return sendPdu(SendingProgressTokenManager.NO_TOKEN, pdu,
                    mTransactionSettings.getMmscUrl());
        }

        /**
         * A common method to send a PDU to MMSC.
         *
         * @param token The token to identify the sending progress.
         * @param pdu A byte array which contains the data of the PDU.
         * @param mmscUrl Url of the recipient MMSC.
         * @return A byte array which contains the response data.
         *         If an HTTP error code is returned, an IOException will be thrown.
         * @throws java.io.IOException if any error occurred on network interface or
         *         an HTTP error code(>=400) returned from the server.
         * @throws com.google.android.mms.MmsException if pdu is null.
         */
        private byte[] sendPdu(final long token, final byte[] pdu,
                               final String mmscUrl) throws IOException, MmsException {
            if (pdu == null) {
                throw new MmsException();
            }

            if (mmscUrl == null) {
                throw new IOException("Cannot establish route: mmscUrl is null");
            }

            if (com.android.mms.transaction.Transaction.useWifi(mContext)) {
                return HttpUtils.httpConnection(
                        mContext, token,
                        mmscUrl,
                        pdu, HttpUtils.HTTP_POST_METHOD,
                        false, null, 0);
            }

            return Utils.ensureRouteToMmsNetwork(mContext, mmscUrl, mTransactionSettings.getProxyAddress(), new Utils.Task<byte[]>() {
                @Override
                public byte[] run() throws IOException {
                    return HttpUtils.httpConnection(
                            mContext, token,
                            mmscUrl,
                            pdu, HttpUtils.HTTP_POST_METHOD,
                            mTransactionSettings.isProxySet(),
                            mTransactionSettings.getProxyAddress(),
                            mTransactionSettings.getProxyPort());
                }
            });
        }

        public abstract void run() throws IOException;
    }

    private static class NotifyRespTask extends CommonNotificationTask {
        NotifyRespTask(Context context, NotificationInd ind, TransactionSettings settings) {
            super(context, settings, ind);
        }

        @Override
        public void run() throws IOException {
            // Create the M-NotifyResp.ind
            NotifyRespInd notifyRespInd = null;
            try {
                notifyRespInd = new NotifyRespInd(
                        PduHeaders.CURRENT_MMS_VERSION,
                        mNotificationInd.getTransactionId(),
                        STATUS_RETRIEVED);

                // Pack M-NotifyResp.ind and send it
                if(com.android.mms.MmsConfig.getNotifyWapMMSC()) {
                    sendPdu(new PduComposer(mContext, notifyRespInd).make(), mContentLocation);
                } else {
                    sendPdu(new PduComposer(mContext, notifyRespInd).make());
                }
            } catch (MmsException e) {
                Log.e(TAG, "error", e);
            }
        }
    }

    private static class AcknowledgeIndTask extends CommonNotificationTask {
        private final RetrieveConf mRetrieveConf;

        AcknowledgeIndTask(Context context, NotificationInd ind, TransactionSettings settings, RetrieveConf rc) {
            super(context, settings, ind);
            mRetrieveConf = rc;
        }

        @Override
        public void run() throws IOException {
            // Send M-Acknowledge.ind to MMSC if required.
            // If the Transaction-ID isn't set in the M-Retrieve.conf, it means
            // the MMS proxy-relay doesn't require an ACK.
            byte[] tranId = mRetrieveConf.getTransactionId();
            if (tranId != null) {
                // Create M-Acknowledge.ind
                com.google.android.mms.pdu_alt.AcknowledgeInd acknowledgeInd = null;
                try {
                    acknowledgeInd = new com.google.android.mms.pdu_alt.AcknowledgeInd(
                            PduHeaders.CURRENT_MMS_VERSION, tranId);

                    // insert the 'from' address per spec
                    String lineNumber = Utils.getMyPhoneNumber(mContext);
                    acknowledgeInd.setFrom(new EncodedStringValue(lineNumber));

                    // Pack M-Acknowledge.ind and send it
                    if(com.android.mms.MmsConfig.getNotifyWapMMSC()) {
                        sendPdu(new PduComposer(mContext, acknowledgeInd).make(), mContentLocation);
                    } else {
                        sendPdu(new PduComposer(mContext, acknowledgeInd).make());
                    }
                } catch (InvalidHeaderValueException e) {
                    Log.e(TAG, "error", e);
                } catch (MmsException e) {
                    Log.e(TAG, "error", e);
                }
            }
        }
    }

    private static CommonNotificationTask getNotificationTask(Context context, Intent intent, byte[] response) {
        if (response.length == 0) {
            return null;
        }

        final GenericPdu pdu =
                (new PduParser(response, new MmsConfig.Overridden(new MmsConfig(context), null).
                        getSupportMmsContentDisposition())).parse();
        if (pdu == null || !(pdu instanceof RetrieveConf)) {
            android.util.Log.e(TAG, "MmsReceivedReceiver.sendNotification failed to parse pdu");
            return null;
        }

        try {
            NotificationInd ind = getNotificationInd(context, intent);
            TransactionSettings transactionSettings = new TransactionSettings(context, null);
            if (intent.getBooleanExtra(EXTRA_TRIGGER_PUSH, false)) {
                return new NotifyRespTask(context, ind, transactionSettings);
            } else {
                return new AcknowledgeIndTask(context, ind, transactionSettings, (RetrieveConf) pdu);
            }
        } catch (MmsException e) {
            Log.e(TAG, "error", e);
            return null;
        }
    }

    private static void executeNotificationTask(CommonNotificationTask task) throws IOException {
        if (task == null) {
            return;
        }

        try {
            // need retry ?
            task.run();
        } catch (IOException e) {
            Log.e(TAG, "MMS send received notification, io exception", e);
            throw e;
        }
    }
}
