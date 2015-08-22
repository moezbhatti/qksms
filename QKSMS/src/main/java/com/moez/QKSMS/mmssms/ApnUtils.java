/*
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
package com.moez.QKSMS.mmssms;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.moez.QKSMS.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApnUtils {
    @SuppressWarnings("unused")
    private static final String TAG = "ApnUtils";
    @SuppressWarnings("unused")
    private static final boolean LOCAL_LOGV = false;

    public static String[] getSimOperatorCodes(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        String simOperator = telephonyManager.getSimOperator();
        String[] mccmnc = new String[]{ null, null };
        if (!TextUtils.isEmpty(simOperator)) {
            mccmnc[0] = simOperator.substring(0, 3);
            mccmnc[1] = simOperator.substring(3);
        }
        return mccmnc;
    }

    public static String[] getNetworkOperatorCodes(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        String simOperator = telephonyManager.getNetworkOperator();
        String[] mccmnc = new String[]{ null, null };
        if (!TextUtils.isEmpty(simOperator)) {
            mccmnc[0] = simOperator.substring(0, 3);
            mccmnc[1] = simOperator.substring(3);
        }
        return mccmnc;
    }

    /**
     * Query for apns using mcc and mnc codes found on the sim card or in the network settings.
     *
     * @param context context
     */
    public static List<Apn> query(Context context) {
        String[] simCodes = getSimOperatorCodes(context);
        String[] networkCodes = getNetworkOperatorCodes(context);

        Set<Apn> resultSet = new HashSet<>();
        resultSet.addAll(query(context, simCodes[0], simCodes[1]));
        resultSet.addAll(query(context, networkCodes[0], networkCodes[1]));

        List<Apn> result = new ArrayList<>(resultSet.size());
        result.addAll(resultSet);
        return result;
    }

    /**
     * Query for apns using mcc and mnc codes found on the sim card or in the network settings.
     *
     * @param mcc mobile country code
     * @param mnc mobile network code
     * @param context context
     */
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public static List<Apn> query(Context context, String mcc, String mnc) {
        ArrayList<Apn> result = new ArrayList<>();

        if (TextUtils.isEmpty(mcc) || TextUtils.isEmpty(mnc)) {
            Log.e(TAG, "Invalid mcc or mnc. {mcc:\"" + mcc + "\", mnc=\"" + mnc + "\"}");
            return result;
        }

        // Scan the apns master list to identify compatible APNs.
        XmlResourceParser parser = context.getResources().getXml(R.xml.apns);
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (isAPNStartTag(parser) && matches(parser, mcc, mnc)) {
                    Apn apn = apnFromParser(parser);
                    // Don't return duplicates!
                    if (!result.contains(apn)) {
                        result.add(apn);
                    }
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException|IOException e) {
            Log.e(TAG, "Exception thrown while getting APNs", e);
        } finally {
            parser.close();
        }

        return result;
    }

    /**
     * Searches the attributes of this tag to determine if
     *
     * 1) this is an mms apn tag; and
     * 2) the mcc and mnc match
     */
    private static boolean matches(XmlPullParser parser, String mcc, String mnc) {
        boolean mccMatches = false;
        boolean mncMatches = false;
        boolean isMMSType = false;
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String name = parser.getAttributeName(i);
            String value = parser.getAttributeValue(i);
            if ("mcc".equals(name) && mcc.equals(value)) {
                mccMatches = true;
            } else if ("mnc".equals(name) && mnc.equals(value)) {
                mncMatches = true;
            } else if ("type".equals(name) &&
                    !TextUtils.isEmpty(value) && value.contains("mms")) {
                isMMSType = true;
            }
        }
        return mccMatches && mncMatches && isMMSType;
    }

    private static Apn apnFromParser(XmlPullParser parser) {
        Apn apn = new Apn();
        String port = null;
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String name = parser.getAttributeName(i);
            String value = parser.getAttributeValue(i);
            switch (name) {
                case "mmsc":
                    apn.mmsc = value;
                    break;
                case "mmsproxy":
                    apn.proxy = value;
                    break;
                case "mmsport":
                    apn.port = value;
                    break;
                case "carrier":
                    apn.name = value;
                    break;
                case "port":
                    port = value;
                    break;
            }
        }

        // Some apn listings don't use the mmsport attribute, but rather the port
        // attribute.
        // grep ' type=".*mms.*"' apns.xml | grep -v 'mmsport' | grep 'port'
        if (TextUtils.isEmpty(apn.port)) {
            apn.port = port;
        }
        return apn;
    }

    private static boolean isAPNStartTag(XmlPullParser parser) throws XmlPullParserException {
        return XmlPullParser.START_TAG == parser.getEventType()
                && "apn".equals(parser.getName());
    }

    /**
     * Saves the APN information to SharedPreferences.
     *
     * @param context context
     * @param apn the apn to save
     */
    public static void persistApn(Context context, Apn apn) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("mmsc_url", apn.mmsc)
                .putString("mms_proxy", apn.proxy)
                .putString("mms_port", apn.port)
                .apply();
    }
}
