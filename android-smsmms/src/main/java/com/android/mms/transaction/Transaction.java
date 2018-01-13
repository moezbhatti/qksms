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
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.android.mms.util.SendingProgressTokenManager;
import com.google.android.mms.MmsException;
import com.klinker.android.send_message.Utils;

/**
 * Transaction is an abstract class for notification transaction, send transaction
 * and other transactions described in MMS spec.
 * It provides the interfaces of them and some common methods for them.
 */
public abstract class Transaction extends Observable {
    private final int mServiceId;

    protected Context mContext;
    protected String mId;
    protected TransactionState mTransactionState;
    protected TransactionSettings mTransactionSettings;

    /**
     * Identifies push requests.
     */
    public static final int NOTIFICATION_TRANSACTION = 0;
    /**
     * Identifies deferred retrieve requests.
     */
    public static final int RETRIEVE_TRANSACTION     = 1;
    /**
     * Identifies send multimedia message requests.
     */
    public static final int SEND_TRANSACTION         = 2;
    /**
     * Identifies send read report requests.
     */
    public static final int READREC_TRANSACTION      = 3;

    public Transaction(Context context, int serviceId,
            TransactionSettings settings) {
        mContext = context;
        mTransactionState = new TransactionState();
        mServiceId = serviceId;
        mTransactionSettings = settings;
    }

    /**
     * Returns the transaction state of this transaction.
     *
     * @return Current state of the Transaction.
     */
    @Override
    public TransactionState getState() {
        return mTransactionState;
    }

    /**
     * An instance of Transaction encapsulates the actions required
     * during a MMS Client transaction.
     */
    public abstract void process();

    /**
     * Used to determine whether a transaction is equivalent to this instance.
     *
     * @param transaction the transaction which is compared to this instance.
     * @return true if transaction is equivalent to this instance, false otherwise.
     */
    public boolean isEquivalent(Transaction transaction) {
        return mId.equals(transaction.mId);
    }

    /**
     * Get the service-id of this transaction which was assigned by the framework.
     * @return the service-id of the transaction
     */
    public int getServiceId() {
        return mServiceId;
    }

    public TransactionSettings getConnectionSettings() {
        return mTransactionSettings;
    }
    public void setConnectionSettings(TransactionSettings settings) {
        mTransactionSettings = settings;
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
    protected byte[] sendPdu(byte[] pdu) throws IOException, MmsException {
        return sendPdu(SendingProgressTokenManager.NO_TOKEN, pdu,
                mTransactionSettings.getMmscUrl());
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
    protected byte[] sendPdu(byte[] pdu, String mmscUrl) throws IOException, MmsException {
        return sendPdu(SendingProgressTokenManager.NO_TOKEN, pdu, mmscUrl);
    }

    /**
     * A common method to send a PDU to MMSC.
     *
     * @param token The token to identify the sending progress.
     * @param pdu A byte array which contains the data of the PDU.
     * @return A byte array which contains the response data.
     *         If an HTTP error code is returned, an IOException will be thrown.
     * @throws java.io.IOException if any error occurred on network interface or
     *         an HTTP error code(>=400) returned from the server.
     * @throws com.google.android.mms.MmsException if pdu is null.
     */
    protected byte[] sendPdu(long token, byte[] pdu) throws IOException, MmsException {
        return sendPdu(token, pdu, mTransactionSettings.getMmscUrl());
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
    protected byte[] sendPdu(final long token, final byte[] pdu,
                             final String mmscUrl) throws IOException, MmsException {
        if (pdu == null) {
            throw new MmsException();
        }

        if (mmscUrl == null) {
            throw new IOException("Cannot establish route: mmscUrl is null");
        }

        if (useWifi(mContext)) {
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

    /**
     * A common method to retrieve a PDU from MMSC.
     *
     * @param url The URL of the message which we are going to retrieve.
     * @return A byte array which contains the data of the PDU.
     *         If the status code is not correct, an IOException will be thrown.
     * @throws java.io.IOException if any error occurred on network interface or
     *         an HTTP error code(>=400) returned from the server.
     */
    protected byte[] getPdu(final String url) throws IOException {
        if (url == null) {
            throw new IOException("Cannot establish route: url is null");
        }

        if (useWifi(mContext)) {
            return HttpUtils.httpConnection(
                    mContext,
                    SendingProgressTokenManager.NO_TOKEN,
                    url,
                    null,
                    HttpUtils.HTTP_GET_METHOD,
                    false,
                    null,
                    0);
        }

        return Utils.ensureRouteToMmsNetwork(mContext, url, mTransactionSettings.getProxyAddress(), new Utils.Task<byte[]>() {
            @Override
            public byte[] run() throws IOException {
                return HttpUtils.httpConnection(
                        mContext,
                        SendingProgressTokenManager.NO_TOKEN,
                        url,
                        null,
                        HttpUtils.HTTP_GET_METHOD,
                        mTransactionSettings.isProxySet(),
                        mTransactionSettings.getProxyAddress(),
                        mTransactionSettings.getProxyPort());
            }
        });
    }

    public static boolean useWifi(Context context) {
        if (Utils.isMmsOverWifiEnabled(context)) {
            ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnMgr != null) {
                NetworkInfo niWF = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if ((niWF != null) && (niWF.isConnected())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": serviceId=" + mServiceId;
    }

    /**
     * Get the type of the transaction.
     *
     * @return Transaction type in integer.
     */
    abstract public int getType();
}
