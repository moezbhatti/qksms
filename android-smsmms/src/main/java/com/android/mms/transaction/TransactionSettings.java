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
import com.klinker.android.logger.Log;

import com.android.mms.MmsConfig;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import com.android.mms.logs.LogTag;

/**
 * Container of transaction settings. Instances of this class are contained
 * within Transaction instances to allow overriding of the default APN
 * settings or of the MMS Client.
 */
public class TransactionSettings {
    private static final String TAG = LogTag.TAG;
    private static final boolean DEBUG = true;
    private static final boolean LOCAL_LOGV = false;

    private String mServiceCenter;
    private String mProxyAddress;
    private int mProxyPort = -1;

    private static final String[] APN_PROJECTION = {
            Telephony.Carriers.TYPE,            // 0
            Telephony.Carriers.MMSC,            // 1
            Telephony.Carriers.MMSPROXY,        // 2
            Telephony.Carriers.MMSPORT          // 3
    };
    private static final int COLUMN_TYPE         = 0;
    private static final int COLUMN_MMSC         = 1;
    private static final int COLUMN_MMSPROXY     = 2;
    private static final int COLUMN_MMSPORT      = 3;

    /**
     * Constructor that uses the default settings of the MMS Client.
     *
     * @param context The context of the MMS Client
     */
    public TransactionSettings(Context context, String apnName) {
        Log.v(TAG, "TransactionSettings: apnName: " + apnName);
//        String selection = "current" + " IS NOT NULL";
//        String[] selectionArgs = null;
//        if (!TextUtils.isEmpty(apnName)) {
//            selection += " AND " + "apn" + "=?";
//            selectionArgs = new String[]{ apnName.trim() };
//        }
//
//        Cursor cursor;
//
//        try {
//            cursor = SqliteWrapper.query(context, context.getContentResolver(),
//                                Telephony.Carriers.CONTENT_URI,
//                                APN_PROJECTION, selection, selectionArgs, null);
//
//                Log.v(TAG, "TransactionSettings looking for apn: " + selection + " returned: " +
//                        (cursor == null ? "null cursor" : (cursor.getCount() + " hits")));
//        } catch (SecurityException e) {
//            Log.e(TAG, "exception thrown", e);
//            cursor = null;
//        }
//
//        if (cursor == null) {
//            Log.e(TAG, "Apn is not found in Database!");
        if (Transaction.settings == null) {
            Transaction.settings = Utils.getDefaultSendSettings(context);
        }

        mServiceCenter = NetworkUtilsHelper.trimV4AddrZeros(Transaction.settings.getMmsc());
        mProxyAddress = NetworkUtilsHelper.trimV4AddrZeros(Transaction.settings.getProxy());

        // Set up the agent, profile url and tag name to be used in the mms request if they are attached in settings
        String agent = Transaction.settings.getAgent();
        if (agent != null && !agent.trim().equals("")) {
            MmsConfig.setUserAgent(agent);
            Log.v(TAG, "set user agent");
        }

        String uaProfUrl = Transaction.settings.getUserProfileUrl();
        if (uaProfUrl != null && !uaProfUrl.trim().equals("")) {
            MmsConfig.setUaProfUrl(uaProfUrl);
            Log.v(TAG, "set user agent profile url");
        }

        String uaProfTagName = Transaction.settings.getUaProfTagName();
        if (uaProfTagName != null && !uaProfTagName.trim().equals("")) {
            MmsConfig.setUaProfTagName(uaProfTagName);
            Log.v(TAG, "set user agent profile tag name");
        }

        if (isProxySet()) {
            try {
                mProxyPort = Integer.parseInt(Transaction.settings.getPort());
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not get proxy: " + Transaction.settings.getPort(), e);
            }
        }
//        }

//        boolean sawValidApn = false;
//        try {
//            while (cursor.moveToNext() && TextUtils.isEmpty(mServiceCenter)) {
//                // Read values from APN settings
//                if (isValidApnType(cursor.getString(COLUMN_TYPE), "mms")) {
//                    sawValidApn = true;
//
//                    String mmsc = cursor.getString(COLUMN_MMSC);
//                    if (mmsc == null) {
//                        continue;
//                    }
//
//                    mServiceCenter = NetworkUtils.trimV4AddrZeros(mmsc.trim());
//                    mProxyAddress = NetworkUtils.trimV4AddrZeros(
//                            cursor.getString(COLUMN_MMSPROXY));
//                    if (isProxySet()) {
//                        String portString = cursor.getString(COLUMN_MMSPORT);
//                        try {
//                            mProxyPort = Integer.parseInt(portString);
//                        } catch (NumberFormatException e) {
//                            if (TextUtils.isEmpty(portString)) {
//                                Log.w(TAG, "mms port not set!");
//                            } else {
//                                Log.e(TAG, "Bad port number format: " + portString, e);
//                            }
//                        }
//                    }
//                }
//            }
//        } finally {
//            cursor.close();
//        }
//
//        Log.v(TAG, "APN setting: MMSC: " + mServiceCenter + " looked for: " + selection);
//
//        if (sawValidApn && TextUtils.isEmpty(mServiceCenter)) {
//            Log.e(TAG, "Invalid APN setting: MMSC is empty");
//        }
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

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "TransactionSettings: " + mServiceCenter +
                    " proxyAddress: " + mProxyAddress +
                    " proxyPort: " + mProxyPort);
        }
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

    static private boolean isValidApnType(String types, String requestType) {
        // If APN type is unspecified, assume APN_TYPE_ALL.
        if (TextUtils.isEmpty(types)) {
            return true;
        }

        for (String t : types.split(",")) {
            if (t.equals(requestType) || t.equals("*")) {
                return true;
            }
        }
        return false;
    }
}
