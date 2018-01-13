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

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Utility to handle phone numbers.
 */
public class PhoneUtils {

    private static final String TAG = "PhoneUtils";

    /**
     * Get a canonical national format phone number. If parsing fails, just return the
     * original number.
     *
     * @param telephonyManager
     * @param subId The SIM ID associated with this number
     * @param phoneText The input phone number text
     * @return The formatted number or the original phone number if failed to parse
     */
    public static String getNationalNumber(TelephonyManager telephonyManager, int subId,
            String phoneText) {
        final String country = getSimOrDefaultLocaleCountry(telephonyManager, subId);
        final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        final Phonenumber.PhoneNumber parsed = getParsedNumber(phoneNumberUtil, phoneText, country);
        if (parsed == null) {
            return phoneText;
        }
        return phoneNumberUtil
                .format(parsed, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
                .replaceAll("\\D", "");
    }

    // Parse the input number into internal format
    private static Phonenumber.PhoneNumber getParsedNumber(PhoneNumberUtil phoneNumberUtil,
            String phoneText, String country) {
        try {
            final Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phoneText, country);
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                return phoneNumber;
            } else {
                Log.e(TAG, "getParsedNumber: not a valid phone number"
                        + " for country " + country);
                return null;
            }
        } catch (final NumberParseException e) {
            Log.e(TAG, "getParsedNumber: Not able to parse phone number");
            return null;
        }
    }

    // Get the country/region either from the SIM ID or from locale
    private static String getSimOrDefaultLocaleCountry(TelephonyManager telephonyManager,
            int subId) {
        String country = getSimCountry(telephonyManager, subId);
        if (TextUtils.isEmpty(country)) {
            country = Locale.getDefault().getCountry();
        }

        return country;
    }

    // Get country/region from SIM ID
    private static String getSimCountry(TelephonyManager telephonyManager, int subId) {
        String country = "";

        try {
            Method method = telephonyManager.getClass().getMethod("getSimCountryIso", int.class);
            country = (String) method.invoke(telephonyManager, subId);
        } catch (Exception e) {
            country = telephonyManager.getSimCountryIso();
        }

        if (TextUtils.isEmpty(country)) {
            return null;
        }
        return country.toUpperCase();
    }
}
