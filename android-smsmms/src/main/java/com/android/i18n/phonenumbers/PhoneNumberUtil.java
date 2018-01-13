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

package com.android.i18n.phonenumbers;

public class PhoneNumberUtil {
    private static PhoneNumberUtil instance;

    public static PhoneNumberUtil getInstance() {
        return instance;
    }

    public String format(Phonenumber.PhoneNumber parsed, PhoneNumberFormat format) {
        return null;
    }

    public Phonenumber.PhoneNumber parse(String s, String s2) throws NumberParseException {
        return new Phonenumber.PhoneNumber();
    }

    public boolean isValidNumber(Phonenumber.PhoneNumber phoneNumber) {
        return true;
    }

    public enum PhoneNumberFormat {
        E164,
        INTERNATIONAL,
        NATIONAL,
        RFC3966
    }
}
