/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.android.mms.transaction;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.telephony.PhoneConstants;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.RateController;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.moez.QKSMS.mmssms.Utils;
import com.moez.QKSMS.R;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The TransactionService of the MMS Client is responsible for handling requests
 * to initiate client-transactions sent from:
 * <ul>
 * <li>The Proxy-Relay (Through Push messages)</li>
 * <li>The composer/viewer activities of the MMS Client (Through intents)</li>
 * </ul>
 * The TransactionService runs locally in the same process as the application.
 * It contains a HandlerThread to which messages are posted from the
 * intent-receivers of this application.
 * <p/>
 * <b>IMPORTANT</b>: This is currently the only instance in the system in
 * which simultaneous connectivity to both the mobile data network and
 * a Wi-Fi network is allowed. This makes the code for handling network
 * connectivity somewhat different than it is in other applications. In
 * particular, we want to be able to send or receive MMS messages when
 * a Wi-Fi connection is active (which implies that there is no connection
 * to the mobile data network). This has two main consequences:
 * <ul>
 * <li>Testing for current network connectivity ({@link android.net.NetworkInfo#isConnected()} is
 * not sufficient. Instead, the correct test is for network availability
 * ({@link android.net.NetworkInfo#isAvailable()}).</li>
 * <li>If the mobile data network is not in the connected state, but it is available,
 * we must initiate setup of the mobile data connection, and defer handling
 * the MMS transaction until the connection is established.</li>
 * </ul>
 */
public class TransactionService extends Service implements Observer {
    private static final String TAG = "TransactionService";
    private static final boolean LOCAL_LOGV = false;

    /**
     * Used to identify notification intents broadcasted by the
     * TransactionService when a Transaction is completed.
     */
    public static final String TRANSACTION_COMPLETED_ACTION =
            "android.intent.action.TRANSACTION_COMPLETED_ACTION";

    /**
     * Action for the Intent which is sent to notify the TransactionService that it should attempt
     * to resend any pending transactions.
     */
    public static final String HANDLE_PENDING_TRANSACTIONS_ACTION =
            "android.intent.action.HANDLE_PENDING_TRANSACTIONS_ACTION";

    /**
     * Action for the Intent which contains TransactionBundle extras. TransactionService uses this
     * to build and process Transactions.
     */
    public static final String TRANSACTION_BUNDLE_ACTION =
            "android.intent.action.TRANSACTION_BUNDLE_ACTION";

    /**
     * Used as extra key in notification intents broadcasted by the TransactionService
     * when a Transaction is completed (TRANSACTION_COMPLETED_ACTION intents).
     * Allowed values for this key are: TransactionState.INITIALIZED,
     * TransactionState.SUCCESS, TransactionState.FAILED.
     */
    public static final String STATE = "state";

    /**
     * Used as extra key in notification intents broadcasted by the TransactionService
     * when a Transaction is completed (TRANSACTION_COMPLETED_ACTION intents).
     * Allowed values for this key are any valid content uri.
     */
    public static final String STATE_URI = "uri";

    private static final int EVENT_TRANSACTION_REQUEST = 1;
    private static final int EVENT_CONTINUE_MMS_CONNECTIVITY = 3;
    private static final int EVENT_HANDLE_NEXT_PENDING_TRANSACTION = 4;
    private static final int EVENT_NEW_INTENT = 5;
    private static final int EVENT_QUIT = 100;

    private static final int TOAST_MSG_QUEUED = 1;
    private static final int TOAST_DOWNLOAD_LATER = 2;
    private static final int TOAST_NO_APN = 3;
    private static final int TOAST_NONE = -1;

    // How often to extend the use of the MMS APN while a transaction
    // is still being processed.
    private static final int APN_EXTENSION_WAIT = 30 * 1000;

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;

    /**
     * Holds Transactions that are being processed.
     *
     * The main purpose of this list is to be able to easily answer the question, "Are we finished
     * processing things?". This is a useful question to answer, since it's expensive to keep the
     * phone awake and to keep MMS connectivity active; we often come to a point where we can stop
     * keeping the phone awake if we knew that we were finished processing everything.
     *
     * A secondary question to answer is, for a given Transaction, "are we already processing this
     * Transaction?".
     *
     * Transactions should be added to this list when they begin being processed, and removed when
     * they are finished or if they fail.
     */
    private final ArrayList<Transaction> mProcessing  = new ArrayList<Transaction>();

    /**
     * Holds deferred Transactions.
     *
     * A Transaction may be deferred if the network isn't available when it comes time to process
     * it. In this case, it will be added to the Pending list and processed either:
     *
     * 1) After another Transaction has been processed; or
     * 2) When the state of the network changes in favour of being able to process the Transaction
     *    (see ConnectivityBroadcastReceiver).
     */
    private final ArrayList<Transaction> mPending  = new ArrayList<Transaction>();

    private ConnectivityManager mConnMgr;
    private ConnectivityBroadcastReceiver mReceiver;
    private boolean mobileDataEnabled;

    private PowerManager.WakeLock mWakeLock;

    /**
     * UI thread handler for displaying toasts.
     */
    public Handler mToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String str = null;

            if (msg.what == TOAST_MSG_QUEUED) {
                str = getString(R.string.message_queued);
            } else if (msg.what == TOAST_DOWNLOAD_LATER) {
                str = getString(R.string.download_later);
            } else if (msg.what == TOAST_NO_APN) {
                str = getString(R.string.no_apn);
            }

            if (str != null) {
                Toast.makeText(TransactionService.this, str,
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onCreate() {
        if (LOCAL_LOGV) Log.v(TAG, "Creating TransactionService");

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        HandlerThread thread = new HandlerThread("TransactionService");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mReceiver = new ConnectivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            // Call onNewIntent (below) on the Transaction thread.
            Message msg = mServiceHandler.obtainMessage(EVENT_NEW_INTENT);
            msg.arg1 = startId;
            msg.obj = intent;
            mServiceHandler.sendMessage(msg);
        }

        // Not sticky, since this service is started when it is needed periodically (as per the
        // recommendation in the docs [1]). I.e. timers are set up to retry downloading messages;
        // the service is started when the user wants to send a message; etc.
        // [1]: http://developer.android.com/reference/android/app/Service.html#START_NOT_STICKY
        return Service.START_NOT_STICKY;
    }

    public void onNewIntent(Intent intent, int serviceId) {
        mobileDataEnabled = Utils.isMobileDataEnabled(this);
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!mobileDataEnabled) {
            Utils.setMobileDataEnabled(this, true);
        }
        if (mConnMgr == null) {
            endMmsConnectivity();
            stopSelf(serviceId);
            return;
        }
        NetworkInfo ni = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
        boolean noNetwork = ni == null || !ni.isAvailable();

        // Make sure we are the default SMS app before processing transactions.
        final String action = intent.getAction();
        if (!Utils.isDefaultSmsApp(this)) {
            // Issue warnings / logs and quit.
            if (TRANSACTION_BUNDLE_ACTION.equals(action)) {
                // This intent only comes from the application developer when they've requested for
                // us to process a Transaction, i.e. a Notification.ind (WAP push) or an M-Send.conf
                // (the user is sending a MM). It is an error to tell us to handle a Transaction
                // when we're not the default, so issue an error log.
                Log.e(TAG, "Asked to process an MMS message when not the default SMS app");
            }

            // HANDLE_PENDING_TRANSACTIONS_ACTION will getConversation sent as part of the framework, so we
            // don't need to issue any warnings.
            if (LOCAL_LOGV) Log.v(TAG, "Stopping TransactionService due to not being the default" +
                                       "SMS app");

            endMmsConnectivity();
            stopSelf(serviceId);
            return;
        }

        if (LOCAL_LOGV) Log.v(TAG, "onNewIntent: serviceId: " + serviceId + ": " + intent.getExtras() +
                    " intent=" + intent);
        if (LOCAL_LOGV) Log.v(TAG, "    networkAvailable=" + !noNetwork);

        if (HANDLE_PENDING_TRANSACTIONS_ACTION.equals(action)) {

            // Scan database to find all pending operations.
            Cursor cursor = PduPersister.getPduPersister(this).getPendingMessages(
                    System.currentTimeMillis());
            if (cursor != null) {
                try {
                    int count = cursor.getCount();

                    if (LOCAL_LOGV) Log.v(TAG, "onNewIntent: cursor.count=" + count + " action=" + action);

                    if (count == 0) {
                        if (LOCAL_LOGV) Log.v(TAG, "onNewIntent: no pending messages. Stopping service.");
                        RetryScheduler.setRetryAlarm(this);
                        stopSelfIfIdle(serviceId);
                        return;
                    }

                    int columnIndexOfMsgId = cursor.getColumnIndexOrThrow("msg_id");
                    int columnIndexOfMsgType = cursor.getColumnIndexOrThrow(
                            "msg_type");

                    while (cursor.moveToNext()) {
                        int msgType = cursor.getInt(columnIndexOfMsgType);
                        int transactionType = getTransactionType(msgType);
                        if (LOCAL_LOGV) Log.v(TAG, "onNewIntent: msgType=" + msgType + " transactionType=" +
                                    transactionType);
                        if (noNetwork) {
                            onNetworkUnavailable(serviceId, transactionType);
                            return;
                        }
                        switch (transactionType) {
                            case -1:
                                break;
                            case Transaction.RETRIEVE_TRANSACTION:
                                // If it's a transiently failed transaction,
                                // we should retry it in spite of current
                                // downloading mode. If the user just turned on the auto-retrieve
                                // option, we also retry those messages that don't have any errors.
                                int failureType = cursor.getInt(
                                        cursor.getColumnIndexOrThrow(
                                                "err_type"));
                                DownloadManager.init(this);
                                DownloadManager downloadManager = DownloadManager.getInstance();
                                boolean autoDownload = downloadManager.isAuto();
                                if (LOCAL_LOGV) Log.v(TAG, "onNewIntent: failureType=" + failureType +
                                            " action=" + action + " isTransientFailure:" +
                                            isTransientFailure(failureType) + " autoDownload=" +
                                            autoDownload);
                                if (!autoDownload) {
                                    // If autodownload is turned off, don't process the
                                    // transaction.
                                    if (LOCAL_LOGV) Log.v(TAG, "onNewIntent: skipping - autodownload off");
                                        //sendBroadcast(new Intent(com.moez.QKSMS.send_message.Transaction.NOTIFY_OF_MMS));
                                    break;
                                }
                                // Logic is twisty. If there's no failure or the failure
                                // is a non-permanent failure, we want to process the transaction.
                                // Otherwise, break out and skip processing this transaction.
                                if (!(failureType == 0 ||
                                        isTransientFailure(failureType))) {
                                    if (LOCAL_LOGV) Log.v(TAG, "onNewIntent: skipping - permanent error");
                                    break;
                                }
                                if (LOCAL_LOGV) Log.v(TAG, "onNewIntent: falling through and processing");
                               // fall-through
                            default:
                                Uri uri = ContentUris.withAppendedId(
                                        Uri.parse("content://mms"),
                                        cursor.getLong(columnIndexOfMsgId));
                                TransactionBundle args = new TransactionBundle(
                                        transactionType, uri.toString());
                                // FIXME: We use the same startId for all MMs.
                                if (LOCAL_LOGV) Log.v(TAG, "onNewIntent: launchTransaction uri=" + uri);
                                launchTransaction(serviceId, args, false);
                                break;
                        }
                    }
                } finally {
                    cursor.close();
                }
            } else {
                if (LOCAL_LOGV) Log.v(TAG, "onNewIntent: no pending messages. Stopping service.");
                RetryScheduler.setRetryAlarm(this);
                stopSelfIfIdle(serviceId);
            }
        } else if (TRANSACTION_BUNDLE_ACTION.equals(action)) {
            if (LOCAL_LOGV) Log.v(TAG, "onNewIntent: launch transaction...");
            // For launching NotificationTransaction and test purpose.
            TransactionBundle args = new TransactionBundle(intent.getExtras());
            launchTransaction(serviceId, args, noNetwork);
        }
    }

    private void stopSelfIfIdle(int startId) {
        synchronized (mProcessing) {
            if (mProcessing.isEmpty() && mPending.isEmpty()) {
                if (LOCAL_LOGV) Log.v(TAG, "stopSelfIfIdle: STOP!");

                stopSelf(startId);
            }
        }
    }

    private static boolean isTransientFailure(int type) {
        return type > 0 && type < 10;
    }

    private int getTransactionType(int msgType) {
        switch (msgType) {
            case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                return Transaction.RETRIEVE_TRANSACTION;
            case PduHeaders.MESSAGE_TYPE_READ_REC_IND:
                return Transaction.READREC_TRANSACTION;
            case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                return Transaction.SEND_TRANSACTION;
            default:
                Log.w(TAG, "Unrecognized MESSAGE_TYPE: " + msgType);
                return -1;
        }
    }

    /**
     * If the network is available, launches the Transaction in the background.
     *
     * TODO: Refactor noNetwork out.
     *
     * @param serviceId used to stop the service when the Transaction is finished processing
     * @param txnBundle information required to build the Transaction from the Intent
     * @param noNetwork true if the MMS network isn't available
     */
    private void launchTransaction(int serviceId, TransactionBundle txnBundle, boolean noNetwork) {
        if (noNetwork) {
            // Display a toast to the user and stop the service.
            onNetworkUnavailable(serviceId, txnBundle.getTransactionType());
            Log.w(TAG, "launchTransaction: no network error!");

        } else {
            // Send the transaction to be processed in the background.
            Message msg = mServiceHandler.obtainMessage(EVENT_TRANSACTION_REQUEST);
            msg.arg1 = serviceId;
            msg.obj = txnBundle;

            if (LOCAL_LOGV) Log.v(TAG, "launchTransaction: sending message " + msg);
            mServiceHandler.sendMessage(msg);
        }
    }

    private void onNetworkUnavailable(int serviceId, int transactionType) {
        if (LOCAL_LOGV) Log.v(TAG, "onNetworkUnavailable: sid=" + serviceId + ", type=" + transactionType);

        // Display a message to the user for retrieve and send transactions.
        int toastType = TOAST_NONE;
        if (transactionType == Transaction.RETRIEVE_TRANSACTION) {
            toastType = TOAST_DOWNLOAD_LATER;
        } else if (transactionType == Transaction.SEND_TRANSACTION) {
            toastType = TOAST_MSG_QUEUED;
        }

        if (toastType != TOAST_NONE) {
            mToastHandler.sendEmptyMessage(toastType);
        }

        // Stop the service.
        stopSelf(serviceId);
    }

    @Override
    public void onDestroy() {
        if (LOCAL_LOGV) Log.v(TAG, "Destroying TransactionService");
        if (!mPending.isEmpty()) {
            Log.w(TAG, "TransactionService exiting with transaction still pending");
        }

        releaseWakeLock();

        unregisterReceiver(mReceiver);

        mServiceHandler.sendEmptyMessage(EVENT_QUIT);

        if (!mobileDataEnabled) {
        if (LOCAL_LOGV) Log.v(TAG, "disabling mobile data");
            Utils.setMobileDataEnabled(TransactionService.this, false);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Handle status change of Transaction (The Observable).
     */
    public void update(Observable observable) {
        Transaction transaction = (Transaction) observable;
        int serviceId = transaction.getServiceId();

        if (LOCAL_LOGV) Log.v(TAG, "update transaction " + serviceId);

        try {
            synchronized (mProcessing) {
                mProcessing.remove(transaction);
                if (mPending.size() > 0) {
                    if (LOCAL_LOGV) Log.v(TAG, "update: handle next pending transaction...");
                    Message msg = mServiceHandler.obtainMessage(
                            EVENT_HANDLE_NEXT_PENDING_TRANSACTION,
                            transaction.getConnectionSettings());
                    mServiceHandler.sendMessage(msg);

                } else if (mProcessing.isEmpty()) {
                    if (LOCAL_LOGV) Log.v(TAG, "update: endMmsConnectivity");
                    endMmsConnectivity();

                } else {
                    if (LOCAL_LOGV) Log.v(TAG, "update: mProcessing is not empty");
                }
            }

            Intent intent = new Intent(TRANSACTION_COMPLETED_ACTION);
            TransactionState state = transaction.getState();
            int result = state.getState();
            intent.putExtra(STATE, result);

            switch (result) {
                case TransactionState.SUCCESS:
                    if (LOCAL_LOGV) Log.v(TAG, "Transaction complete: " + serviceId);

                    intent.putExtra(STATE_URI, state.getContentUri());

                    // Notify user in the system-wide notification area.
                    switch (transaction.getType()) {
                        case Transaction.NOTIFICATION_TRANSACTION:
                        case Transaction.RETRIEVE_TRANSACTION:
                            if (LOCAL_LOGV) Log.v(TAG, "I removed some stuff here...");
                            break;
                        case Transaction.SEND_TRANSACTION:
                            RateController.getInstance().update();
                            break;
                    }
                    break;
                case TransactionState.FAILED:
                    if (LOCAL_LOGV) Log.v(TAG, "Transaction failed: " + serviceId);
                    break;
                default:
                    if (LOCAL_LOGV) Log.v(TAG, "Transaction state unknown: " +
                                serviceId + " " + result);
                    break;
            }

            if (LOCAL_LOGV) Log.v(TAG, "update: broadcast transaction result " + result);
            // Broadcast the result of the transaction.
            sendBroadcast(intent);
        } finally {
            transaction.detach(this);
            stopSelfIfIdle(serviceId);
        }
    }

    private synchronized void createWakeLock() {
        // Create a new wake lock if we haven't made one yet.
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MMS Connectivity");
            mWakeLock.setReferenceCounted(false);
        }
    }

    private void acquireWakeLock() {
        // It's okay to double-acquire this because we are not using it
        // in reference-counted mode.
        if (LOCAL_LOGV) Log.v(TAG, "mms acquireWakeLock");
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        // Don't release the wake lock if it hasn't been created and acquired.
        if (mWakeLock != null && mWakeLock.isHeld()) {
            if (LOCAL_LOGV) Log.v(TAG, "mms releaseWakeLock");
            mWakeLock.release();
        }
    }

    /**
     *
     * @return the PhoneConstants.APN_* result code from calling
     *         ConnectionManager#startUsingNetworkFeature with "enableMMS".
     * @throws IOException
     */
    protected int beginMmsConnectivity() throws IOException {
        if (LOCAL_LOGV) Log.v(TAG, "beginMmsConnectivity");
        // Take a wake lock so we don't fall asleep before the message is downloaded.
        createWakeLock();

        int result = mConnMgr.startUsingNetworkFeature(
                ConnectivityManager.TYPE_MOBILE, "enableMMS");

        if (LOCAL_LOGV) Log.v(TAG, "beginMmsConnectivity: result=" + result);

        switch (result) {
            case PhoneConstants.APN_ALREADY_ACTIVE:
            case PhoneConstants.APN_REQUEST_STARTED:
                acquireWakeLock();
                return result;
        }

        throw new IOException("Cannot establish MMS connectivity");
    }

    protected void endMmsConnectivity() {
        try {
            if (LOCAL_LOGV) Log.v(TAG, "endMmsConnectivity");

            // cancel timer for renewal of lease
            mServiceHandler.removeMessages(EVENT_CONTINUE_MMS_CONNECTIVITY);
            if (mConnMgr != null) {
                mConnMgr.stopUsingNetworkFeature(
                        ConnectivityManager.TYPE_MOBILE,
                        "enableMMS");
            }
        } finally {
            releaseWakeLock();
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        private String decodeMessage(Message msg) {
            if (msg.what == EVENT_QUIT) {
                return "EVENT_QUIT";
            } else if (msg.what == EVENT_CONTINUE_MMS_CONNECTIVITY) {
                return "EVENT_CONTINUE_MMS_CONNECTIVITY";
            } else if (msg.what == EVENT_TRANSACTION_REQUEST) {
                return "EVENT_TRANSACTION_REQUEST";
            } else if (msg.what == EVENT_HANDLE_NEXT_PENDING_TRANSACTION) {
                return "EVENT_HANDLE_NEXT_PENDING_TRANSACTION";
            } else if (msg.what == EVENT_NEW_INTENT) {
                return "EVENT_NEW_INTENT";
            }
            return "unknown message.what";
        }

        private String decodeTransactionType(int transactionType) {
            if (transactionType == Transaction.NOTIFICATION_TRANSACTION) {
                return "NOTIFICATION_TRANSACTION";
            } else if (transactionType == Transaction.RETRIEVE_TRANSACTION) {
                return "RETRIEVE_TRANSACTION";
            } else if (transactionType == Transaction.SEND_TRANSACTION) {
                return "SEND_TRANSACTION";
            } else if (transactionType == Transaction.READREC_TRANSACTION) {
                return "READREC_TRANSACTION";
            }
            return "invalid transaction type";
        }

        /**
         * Handle incoming transaction requests.
         * The incoming requests are initiated by the MMSC Server or by the
         * MMS Client itself.
         */
        @Override
        public void handleMessage(Message msg) {
            if (LOCAL_LOGV) Log.v(TAG, "Handling incoming message: " + msg + " = " + decodeMessage(msg));

            Transaction transaction = null;

            switch (msg.what) {
                case EVENT_NEW_INTENT:
                    onNewIntent((Intent)msg.obj, msg.arg1);
                    break;

                case EVENT_QUIT:
                    getLooper().quit();
                    return;

                case EVENT_CONTINUE_MMS_CONNECTIVITY:

                    // If there are no items that are processing, then quit.
                    synchronized (mProcessing) {
                        if (mProcessing.isEmpty()) {
                            return;
                        }
                    }

                    if (LOCAL_LOGV) Log.v(TAG, "handle EVENT_CONTINUE_MMS_CONNECTIVITY event...");

                    try {
                        int result = beginMmsConnectivity();
                        if (result != PhoneConstants.APN_ALREADY_ACTIVE) {
                        if (LOCAL_LOGV) Log.v(TAG, "Extending MMS connectivity returned " + result +
                                    " instead of APN_ALREADY_ACTIVE");
                            // Just wait for connectivity startup without
                            // any new request of APN switch.
                            return;
                        }
                    } catch (IOException e) {
                        Log.w(TAG, "Attempt to extend use of MMS connectivity failed");
                        return;
                    }

                    // Restart timer
                    renewMmsConnectivity();
                    return;

                case EVENT_TRANSACTION_REQUEST:

                    // At the end of this block, if transaction is null then the service will be
                    // stopped.
                    int serviceId = msg.arg1;
                    try {
                        TransactionBundle args = (TransactionBundle) msg.obj;
                        TransactionSettings transactionSettings;

                        if (LOCAL_LOGV) Log.v(TAG, "EVENT_TRANSACTION_REQUEST MmscUrl=" +
                                    args.getMmscUrl() + " proxy port: " + args.getProxyAddress());

                        // Set the connection settings for this transaction.
                        // If these have not been set in args, load the default settings.
                        String mmsc = args.getMmscUrl();
                        if (mmsc != null) {
                            transactionSettings = new TransactionSettings(
                                    mmsc, args.getProxyAddress(), args.getProxyPort());
                        } else {
                            transactionSettings = new TransactionSettings(
                                                    TransactionService.this, null);
                        }

                        int transactionType = args.getTransactionType();

                        if (LOCAL_LOGV) Log.v(TAG, "handle EVENT_TRANSACTION_REQUEST: transactionType=" +
                                    transactionType + " " + decodeTransactionType(transactionType));

                        // Create appropriate transaction
                        switch (transactionType) {
                            case Transaction.NOTIFICATION_TRANSACTION:
                                String uri = args.getUri();
                                if (uri != null) {
                                    transaction = new NotificationTransaction(
                                            TransactionService.this, serviceId,
                                            transactionSettings, uri);
                                } else {
                                    // Now it's only used for test purpose.
                                    byte[] pushData = args.getPushData();
                                    PduParser parser = new PduParser(pushData);
                                    GenericPdu ind = parser.parse();

                                    int type = PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
                                    if ((ind != null) && (ind.getMessageType() == type)) {
                                        transaction = new NotificationTransaction(
                                                TransactionService.this, serviceId,
                                                transactionSettings, (NotificationInd) ind);
                                    } else {
                                        Log.e(TAG, "Invalid PUSH data.");
                                        transaction = null;
                                        return;
                                    }
                                }
                                break;
                            case Transaction.RETRIEVE_TRANSACTION:
                                transaction = new RetrieveTransaction(
                                        TransactionService.this, serviceId,
                                        transactionSettings, args.getUri());
                                break;
                            case Transaction.SEND_TRANSACTION:
                                transaction = new SendTransaction(
                                        TransactionService.this, serviceId,
                                        transactionSettings, args.getUri());
                                break;
                            case Transaction.READREC_TRANSACTION:
                                transaction = new ReadRecTransaction(
                                        TransactionService.this, serviceId,
                                        transactionSettings, args.getUri());
                                break;
                            default:
                                Log.w(TAG, "Invalid transaction type: " + serviceId);
                                // Set transaction to null to stop the service.
                                transaction = null;
                                return;
                        }

                        if (!processTransaction(transaction)) {
                            // Set transaction to null to stop the service.
                            transaction = null;
                            return;
                        }

                        if (LOCAL_LOGV) Log.v(TAG, "Started processing of incoming message: " + msg);

                    } catch (Exception ex) {
                        Log.w(TAG, "Exception occurred while handling message: " + msg, ex);

                        if (transaction != null) {
                            try {
                                transaction.detach(TransactionService.this);
                                synchronized (mProcessing) {
                                    if (mProcessing.contains(transaction)) {
                                        mProcessing.remove(transaction);
                                    }
                                }
                            } catch (Throwable t) {
                                Log.e(TAG, "Unexpected Throwable.", t);
                            } finally {
                                // Set transaction to null to stop the service.
                                transaction = null;
                            }
                        }
                    } finally {
                        if (transaction == null) {
                            if (LOCAL_LOGV) Log.v(TAG, "Transaction was null. Stopping self: " + serviceId);
                            // Transaction was null, so stop the service.
                            endMmsConnectivity();
                            stopSelf(serviceId);
                        }
                    }
                    return;
                case EVENT_HANDLE_NEXT_PENDING_TRANSACTION:
                    processPendingTransaction((TransactionSettings) msg.obj);
                    return;
                default:
                    Log.w(TAG, "what=" + msg.what);
                    return;
            }
        }

        public void markAllPendingTransactionsAsFailed() {
            synchronized (mProcessing) {
                while (mPending.size() != 0) {
                    Transaction transaction = mPending.remove(0);
                    transaction.mTransactionState.setState(TransactionState.FAILED);
                    if (transaction instanceof SendTransaction) {
                        Uri uri = ((SendTransaction)transaction).mSendReqURI;
                        transaction.mTransactionState.setContentUri(uri);
                        int respStatus = PduHeaders.RESPONSE_STATUS_ERROR_NETWORK_PROBLEM;
                        ContentValues values = new ContentValues(1);
                        values.put("resp_st", respStatus);

                        SqliteWrapper.update(TransactionService.this,
                                TransactionService.this.getContentResolver(),
                                uri, values, null, null);
                    }
                    transaction.notifyObservers();
                }
            }
        }

        public void processPendingTransaction(TransactionSettings settings) {

            Transaction transaction = null;
            int numProcessTransaction;

            synchronized (mProcessing) {
                if (mPending.size() != 0) {
                    transaction = mPending.remove(0);
                }
                numProcessTransaction = mProcessing.size();
            }

            if (transaction != null) {
                if (settings != null) {
                    transaction.setConnectionSettings(settings);
                }

                /*
                 * Process deferred transaction
                 */
                try {
                    int serviceId = transaction.getServiceId();

                    if (LOCAL_LOGV) Log.v(TAG, "processPendingTxn: process " + serviceId);

                    if (processTransaction(transaction)) {
                        if (LOCAL_LOGV) Log.v(TAG, "Started deferred processing of transaction  "
                                    + transaction);
                    } else {
                        stopSelf(serviceId);
                    }
                } catch (IOException e) {
                    Log.w(TAG, e.getMessage(), e);
                }
            } else {
                if (numProcessTransaction == 0) {
                    if (LOCAL_LOGV) Log.v(TAG, "processPendingTxn: no more transaction, endMmsConnectivity");
                    endMmsConnectivity();
                }
            }
        }

        /**
         * Internal method to begin processing a transaction.
         * @param transaction the transaction. Must not be {@code null}.
         * @return {@code true} if process has begun or will begin. {@code false}
         * if the transaction should be discarded.
         * @throws java.io.IOException if connectivity for MMS traffic could not be
         * established.
         */
        private boolean processTransaction(Transaction transaction) throws IOException {
            // Check if transaction already processing
            synchronized (mProcessing) {
                for (Transaction t : mPending) {
                    if (t.isEquivalent(transaction)) {
                        if (LOCAL_LOGV) Log.v(TAG, "Transaction already pending: " +
                                    transaction.getServiceId());
                        return true;
                    }
                }
                for (Transaction t : mProcessing) {
                    if (t.isEquivalent(transaction)) {
                        if (LOCAL_LOGV) Log.v(TAG, "Duplicated transaction: " + transaction.getServiceId());
                        return true;
                    }
                }

                // Make sure that the network connectivity necessary for MMS traffic is enabled. If
                // it is not, we need to defer processing the transaction until connectivity is
                // established.
                if (LOCAL_LOGV) Log.v(TAG, "processTransaction: call beginMmsConnectivity...");
                int connectivityResult = beginMmsConnectivity();
                if (connectivityResult == PhoneConstants.APN_REQUEST_STARTED) {
                    mPending.add(transaction);
                    if (LOCAL_LOGV) Log.v(TAG, "processTransaction: connResult=APN_REQUEST_STARTED, " +
                                "defer transaction pending MMS connectivity");
                    return true;
                }

                if (LOCAL_LOGV) Log.v(TAG, "Adding transaction to 'mProcessing' list: " + transaction);
                mProcessing.add(transaction);
            }

            // Set a timer to keep renewing our "lease" on the MMS connection
            sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY),
                               APN_EXTENSION_WAIT);

            if (LOCAL_LOGV) Log.v(TAG, "processTransaction: starting transaction " + transaction);

            // Attach to transaction and process it
            transaction.attach(TransactionService.this);
            transaction.process();
            return true;
        }
    }

    private void renewMmsConnectivity() {
        // Set a timer to keep renewing our "lease" on the MMS connection
        mServiceHandler.sendMessageDelayed(
                mServiceHandler.obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY),
                           APN_EXTENSION_WAIT);
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LOCAL_LOGV) Log.v(TAG, "ConnectivityBroadcastReceiver.onReceive() action: " + action);

            // "A change in network connectivity has occurred. A default connection has either
            // been established or lost."
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                NetworkInfo mmsNetworkInfo = null;
                boolean mobileDataEnabled = Utils.isMobileDataEnabled(context);

                if (mConnMgr != null && mobileDataEnabled) {
                    mmsNetworkInfo = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
                } else {
                    if (LOCAL_LOGV) Log.v(TAG, "mConnMgr is null, bail");
                }

                // If we are being informed that connectivity has been established
                // to allow MMS traffic, then proceed with processing the pending
                // transaction, if any.
                if (LOCAL_LOGV) Log.v(TAG, "Handle ConnectivityBroadcastReceiver.onReceive(): " + mmsNetworkInfo);

                // Check availability of the mobile network.
                if (mmsNetworkInfo == null) {
                    if (LOCAL_LOGV) Log.v(TAG, "mms type is null or mobile data is turned off, bail");
                } else {
                    // This is a very specific fix to handle the case where the phone receives an
                    // incoming call during the time we're trying to setup the mms connection.
                    // When the call ends, restart the process of mms connectivity.
                    if ("2GVoiceCallEnded".equals(mmsNetworkInfo.getReason())) {
                        if (LOCAL_LOGV) Log.v(TAG, "   reason is 2GVoiceCallEnded, retrying mms connectivity");
                        renewMmsConnectivity();

                    } else if (mmsNetworkInfo.isConnected() ||
                            (mmsNetworkInfo.getState().equals(NetworkInfo.State.UNKNOWN) &&
                             mmsNetworkInfo.isAvailable())) {

                        TransactionSettings settings = new TransactionSettings(
                                TransactionService.this, mmsNetworkInfo.getExtraInfo());
                        mServiceHandler.processPendingTransaction(settings);

                    } else {
                        if (LOCAL_LOGV) Log.v(TAG, "   TYPE_MOBILE_MMS not connected, bail");
                        // Retry mms connectivity once it's possible to connect
                        if (mmsNetworkInfo.isAvailable()) {
                            if (LOCAL_LOGV) Log.v(TAG, "   retrying mms connectivity for it's available");
                            renewMmsConnectivity();
                        }
                    }
                }
            }
        }
    }
}
