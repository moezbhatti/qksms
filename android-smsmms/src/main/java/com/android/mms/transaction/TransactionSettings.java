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

import android.content.Context;
import android.net.NetworkUtilsHelper;
import android.provider.Telephony;
import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;
import timber.log.Timber;

/**
 * Container of transaction settings. Instances of this class are contained
 * within Transaction instances to allow overriding of the default APN
 * settings or of the MMS Client.
 */
public class TransactionSettings {
    private String mServiceCenter;
    private String mProxyAddress;
    private int mProxyPort = -1;

    /**
     * Constructor that uses the default settings of the MMS Client.
     *
     * @param context The context of the MMS Client
     */
    public TransactionSettings(Context context, String apnName) {
        Timber.v("TransactionSettings: apnName: " + apnName);
        if (Transaction.Companion.getSettings() == null) {
            Transaction.Companion.setSettings(Utils.getDefaultSendSettings(context));
        }

        mServiceCenter = NetworkUtilsHelper.trimV4AddrZeros(Transaction.Companion.getSettings().getMmsc());
        mProxyAddress = NetworkUtilsHelper.trimV4AddrZeros(Transaction.Companion.getSettings().getProxy());

        // Set up the agent, profile url and tag name to be used in the mms request if they are attached in settings
        String agent = Transaction.Companion.getSettings().getAgent();
        if (agent != null && !agent.trim().equals("")) {
            MmsConfig.setUserAgent(agent);
            Timber.v("set user agent");
        }

        String uaProfUrl = Transaction.Companion.getSettings().getUserProfileUrl();
        if (uaProfUrl != null && !uaProfUrl.trim().equals("")) {
            MmsConfig.setUaProfUrl(uaProfUrl);
            Timber.v("set user agent profile url");
        }

        String uaProfTagName = Transaction.Companion.getSettings().getUaProfTagName();
        if (uaProfTagName != null && !uaProfTagName.trim().equals("")) {
            MmsConfig.setUaProfTagName(uaProfTagName);
            Timber.v("set user agent profile tag name");
        }

        if (isProxySet()) {
            try {
                mProxyPort = Integer.parseInt(Transaction.Companion.getSettings().getPort());
            } catch (NumberFormatException e) {
                Timber.e(e, "could not get proxy: " + Transaction.Companion.getSettings().getPort());
            }
        }
    }

    /**
     * Constructor that overrides the default settings of the MMS Client.
     *
     * @param mmscUrl The MMSC URL
     * @param proxyAddr The proxy address
     * @param proxyPort The port used by the proxy address
     * immediately start a SendTransaction upon completion of a NotificationTransaction,
     * false otherwise.
     */
    public TransactionSettings(String mmscUrl, String proxyAddr, int proxyPort) {
        mServiceCenter = mmscUrl != null ? mmscUrl.trim() : null;
        mProxyAddress = proxyAddr;
        mProxyPort = proxyPort;

            Timber.v("TransactionSettings: " + mServiceCenter
                    + " proxyAddress: " + mProxyAddress
                    + " proxyPort: " + mProxyPort);
   }

    public String getMmscUrl() {
        return mServiceCenter;
    }

    public String getProxyAddress() {
        return mProxyAddress;
    }

    public int getProxyPort() {
        return mProxyPort;
    }

    public boolean isProxySet() {
        return (mProxyAddress != null) && (mProxyAddress.trim().length() != 0);
    }

}
