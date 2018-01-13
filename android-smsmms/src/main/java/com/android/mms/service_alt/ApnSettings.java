/*
 * Copyright (C) 2015 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.service_alt;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.NetworkUtilsHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.text.TextUtils;
import com.klinker.android.logger.Log;

import com.android.mms.service_alt.exception.ApnException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * APN settings used for MMS transactions
 */
public class ApnSettings {
    private static final String TAG = "ApnSettings";

    // MMSC URL
    private final String mServiceCenter;
    // MMSC proxy address
    private final String mProxyAddress;
    // MMSC proxy port
    private final int mProxyPort;
    // Debug text for this APN: a concatenation of interesting columns of this APN
    private final String mDebugText;

    private static final String[] APN_PROJECTION = {
            Telephony.Carriers.TYPE,
            Telephony.Carriers.MMSC,
            Telephony.Carriers.MMSPROXY,
            Telephony.Carriers.MMSPORT,
            Telephony.Carriers.NAME,
            Telephony.Carriers.APN,
            Telephony.Carriers.BEARER,
            Telephony.Carriers.PROTOCOL,
            Telephony.Carriers.ROAMING_PROTOCOL,
            Telephony.Carriers.AUTH_TYPE,
            Telephony.Carriers.MVNO_TYPE,
            Telephony.Carriers.MVNO_MATCH_DATA,
            Telephony.Carriers.PROXY,
            Telephony.Carriers.PORT,
            Telephony.Carriers.SERVER,
            Telephony.Carriers.USER,
            Telephony.Carriers.PASSWORD,
    };
    private static final int COLUMN_TYPE         = 0;
    private static final int COLUMN_MMSC         = 1;
    private static final int COLUMN_MMSPROXY     = 2;
    private static final int COLUMN_MMSPORT      = 3;
    private static final int COLUMN_NAME         = 4;
    private static final int COLUMN_APN          = 5;
    private static final int COLUMN_BEARER       = 6;
    private static final int COLUMN_PROTOCOL     = 7;
    private static final int COLUMN_ROAMING_PROTOCOL = 8;
    private static final int COLUMN_AUTH_TYPE    = 9;
    private static final int COLUMN_MVNO_TYPE    = 10;
    private static final int COLUMN_MVNO_MATCH_DATA = 11;
    private static final int COLUMN_PROXY        = 12;
    private static final int COLUMN_PORT         = 13;
    private static final int COLUMN_SERVER       = 14;
    private static final int COLUMN_USER         = 15;
    private static final int COLUMN_PASSWORD     = 16;


    /**
     * Load APN settings from system
     *
     * @param context
     * @param apnName the optional APN name to match
     */
    public static ApnSettings load(Context context, String apnName, int subId)
            throws ApnException {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String mmsc = sharedPrefs.getString("mmsc_url", "");
        if (!TextUtils.isEmpty(mmsc)) {
            String mmsProxy = sharedPrefs.getString("mms_proxy", "");
            String mmsPort = sharedPrefs.getString("mms_port", "");
            return new ApnSettings(mmsc, mmsProxy, parsePort(mmsPort), "Default from settings");
        }

        Log.v(TAG, "ApnSettings: apnName " + apnName);
        // TODO: CURRENT semantics is currently broken in telephony. Revive this when it is fixed.
        //String selection = Telephony.Carriers.CURRENT + " IS NOT NULL";
        String selection = null;
        String[] selectionArgs = null;
        apnName = apnName != null ? apnName.trim() : null;
        if (!TextUtils.isEmpty(apnName)) {
            //selection += " AND " + Telephony.Carriers.APN + "=?";
            selection = Telephony.Carriers.APN + "=?";
            selectionArgs = new String[]{ apnName };
        }
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(
                    context,
                    context.getContentResolver(),
                    Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "/subId/" + subId),
                    APN_PROJECTION,
                    selection,
                    selectionArgs,
                    null/*sortOrder*/);
            if (cursor != null) {
                String mmscUrl = null;
                String proxyAddress = null;
                int proxyPort = -1;
                while (cursor.moveToNext()) {
                    // Read values from APN settings
                    if (isValidApnType(
                            cursor.getString(COLUMN_TYPE), "mms")) {
                        mmscUrl = trimWithNullCheck(cursor.getString(COLUMN_MMSC));
                        if (TextUtils.isEmpty(mmscUrl)) {
                            continue;
                        }
                        mmscUrl = NetworkUtilsHelper.trimV4AddrZeros(mmscUrl);
                        try {
                            new URI(mmscUrl);
                        } catch (URISyntaxException e) {
                            throw new ApnException("Invalid MMSC url " + mmscUrl);
                        }
                        proxyAddress = trimWithNullCheck(cursor.getString(COLUMN_MMSPROXY));
                        if (!TextUtils.isEmpty(proxyAddress)) {
                            proxyAddress = NetworkUtilsHelper.trimV4AddrZeros(proxyAddress);
                            final String portString =
                                    trimWithNullCheck(cursor.getString(COLUMN_MMSPORT));
                            if (portString != null) {
                                try {
                                    proxyPort = Integer.parseInt(portString);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid port " + portString);
                                    throw new ApnException("Invalid port " + portString);
                                }
                            }
                        }
                        return new ApnSettings(
                                mmscUrl, proxyAddress, proxyPort, getDebugText(cursor));
                    }
                }

            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return new ApnSettings("", "", 80, "Failed to find APNs :(");
    }

    private static String getDebugText(Cursor cursor) {
        final StringBuilder sb = new StringBuilder();
        sb.append("APN [");
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            final String name = cursor.getColumnName(i);
            final String value = cursor.getString(i);
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(name).append('=').append(value);
        }
        sb.append("]");
        return sb.toString();
    }

    private static String trimWithNullCheck(String value) {
        return value != null ? value.trim() : null;
    }

    public ApnSettings(String mmscUrl, String proxyAddr, int proxyPort, String debugText) {
        mServiceCenter = mmscUrl;
        mProxyAddress = proxyAddr;
        mProxyPort = proxyPort;
        mDebugText = debugText;
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
        return !TextUtils.isEmpty(mProxyAddress);
    }

    private static boolean isValidApnType(String types, String requestType) {
        // If APN type is unspecified, assume APN_TYPE_ALL.
        if (TextUtils.isEmpty(types)) {
            return true;
        }
        for (String type : types.split(",")) {
            type = type.trim();
            if (type.equals(requestType) || type.equals("*")) {
                return true;
            }
        }
        return false;
    }

    private static int parsePort(String port) {
        if (TextUtils.isEmpty(port)) {
            return 80;
        } else {
            return Integer.parseInt(port);
        }
    }

    public String toString() {
        return mDebugText;
    }
}
