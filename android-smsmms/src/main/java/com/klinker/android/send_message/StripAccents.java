/*
 * Copyright 2013 Jacob Klinker
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

package com.klinker.android.send_message;

import android.telephony.SmsMessage;

public class StripAccents {

    public static String characters = "\u03B1\u03B2\u03B3\u03B4\u03B5\u03B6\u03B7\u03B8\u03B9\u03BA\u03BB\u03BC\u03BD" +
            "\u03BE\u03BF\u03C0\u03C1\u03C3\u03C2\u03C4\u03C5\u03C6\u03C7\u03C8\u03C9\u03AC\u03AD" +
            "\u03AE\u03AF\u03CC\u03CD\u03CE\u03CA\u03CB\u0390\u03B0\u0391\u0392\u0395\u0396\u0397\u0399" +
            "\u039A\u039C\u039D\u039F\u03A1\u03A4\u03A5\u03A7\u0386\u0388\u0389\u038A\u038C\u038F\u03AA" +
            "\u03AB\u0170\u0171\u0150\u0151\u0105\u0107\u0119\u0142\u0144\u015B\u017A\u017C\u0104\u0106" +
            "\u0118\u0141\u0143\u015A\u0179\u017B\u00C0\u00C2\u00C3\u00C8\u00CA\u00CC\u00CE\u00D2\u00D5" +
            "\u00D9\u00DB\u00E2\u00E3\u00EA\u00EE\u00F5\u00FA\u00FB\u00E7\u011B\u0161\u010D\u0159\u017E\u010F" +
            "\u0165\u0148\u00E1\u00ED\u00E9\u00F3\u00FD\u016F\u011A\u0160\u010C\u0158\u017D\u010E\u0164\u0147" +
            "\u00C1\u00C9\u00CD\u00D3\u00DD\u00DA\u016E\u0155\u013A\u013E\u00F4\u0154\u0139\u013D\u00D4\u00CF\u00EF\u00EB\u00CB";

    public static String gsm = "AB\u0393\u0394EZH\u0398IK\u039BMN\u039EO\u03A0P\u03A3\u03A3TY\u03A6X\u03A8\u03A9AEHIOY" +
            "\u03A9IYIYABEZHIKMNOPTYXAEHIO\u03A9IY\u00DC\u00FC\u00D6\u00F6acelnszzACELNSZZAAAEEIIOOUU" +
            "aaeiouucescrzdtnaieoyuESCRZDTNAEIOYUUrlloRLLOIIee";

    public static String stripAccents(String s) {
        int[] messageData = SmsMessage.calculateLength(s, false);

        if (messageData[0] != 1) {
            for (int i = 0; i < characters.length(); i++) {
                s = s.replaceAll(characters.substring(i, i + 1), gsm.substring(i, i + 1));
            }
        }

        return s;
    }
}
