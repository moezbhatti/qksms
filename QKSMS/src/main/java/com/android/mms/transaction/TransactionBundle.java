/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

import android.os.Bundle;

/**
 * A wrapper around the Bundle instances used to start the TransactionService.
 * It provides high-level APIs to set the information required for the latter to
 * instantiate transactions.
 */
public class TransactionBundle {
    /**
     * Key for the transaction-type.
     * Allowed values for this key are: TYPE_PUSH_TRANSACTION, TYPE_RETRIEVE_TRANSACTION,
     * TYPE_SEND_TRANSACTION, and TYPE_READREC_TRANSACTION.
     */
    public static final String TRANSACTION_TYPE = "type";

    /**
     * Key of the push-data.
     * Used when TRANSACTION_TYPE is TYPE_PUSH_TRANSACTION.
     */
    private static final String PUSH_DATA = "mms-push-data";

    /**
     * Key of the MMSC server URL.
     */
    private static final String MMSC_URL = "mmsc-url";

    /**
     * Key of the HTTP Proxy address.
     */
    private static final String PROXY_ADDRESS = "proxy-address";

    /**
     * Key of the HTTP Proxy port.
     */
    private static final String PROXY_PORT = "proxy-port";

    /**
     * Key of the URI.
     * Indicates the URL of the M-Retrieve.conf in TYPE_RETRIEVE_TRANSACTION, or the
     * Uri of the M-Send.req/M-Read-Rec.ind in TYPE_SEND_TRANSACTION and
     * TYPE_READREC_TRANSACTION, respectively.
     */
    public static final String URI = "uri";

    /**
     * This is the real Bundle to be sent to the TransactionService upon calling
     * startService.
     */
    private final Bundle mBundle;

    /**
     * Private constructor.
     *
     * @param transactionType
     */
    private TransactionBundle(int transactionType) {
        mBundle = new Bundle();
        mBundle.putInt(TRANSACTION_TYPE, transactionType);
    }

    /**
     * Constructor of a bundle used for TransactionBundle instances of type
     * TYPE_RETRIEVE_TRANSACTION, TYPE_SEND_TRANSACTION, and TYPE_READREC_TRANSACTION.
     *
     * @param transactionType
     * @param uri The relevant URI for this transaction. Indicates the URL of the
     * M-Retrieve.conf in TYPE_RETRIEVE_TRANSACTION, or the Uri of the
     * M-Send.req/M-Read-Rec.ind in TYPE_SEND_TRANSACTION and
     * TYPE_READREC_TRANSACTION, respectively.
     */
    public TransactionBundle(int transactionType, String uri) {
        this(transactionType);
        mBundle.putString(URI, uri);
    }

    /**
     * Constructor of a transaction bundle used for incoming bundle instances.
     *
     * @param bundle The incoming bundle
     */
    public TransactionBundle(Bundle bundle) {
        mBundle = bundle;
    }

    public void setConnectionSettings(String mmscUrl,
            String proxyAddress,
            int proxyPort) {
        mBundle.putString(MMSC_URL, mmscUrl);
        mBundle.putString(PROXY_ADDRESS, proxyAddress);
        mBundle.putInt(PROXY_PORT, proxyPort);
    }

    public void setConnectionSettings(TransactionSettings settings) {
        setConnectionSettings(
                settings.getMmscUrl(),
                settings.getProxyAddress(),
                settings.getProxyPort());
    }

    public Bundle getBundle() {
        return mBundle;
    }

    public int getTransactionType() {
        return mBundle.getInt(TRANSACTION_TYPE);
    }

    public String getUri() {
        return mBundle.getString(URI);
    }

    public byte[] getPushData() {
        return mBundle.getByteArray(PUSH_DATA);
    }

    public String getMmscUrl() {
        return mBundle.getString(MMSC_URL);
    }

    public String getProxyAddress() {
        return mBundle.getString(PROXY_ADDRESS);
    }

    public int getProxyPort() {
        return mBundle.getInt(PROXY_PORT);
    }
}
