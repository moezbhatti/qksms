/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package util

import android.text.TextUtils
import android.util.Patterns
import java.util.regex.Pattern

/**
 * Utils for using Telephony functions on lower API levels
 *
 * Adapted from https://android.googlesource.com/platform/packages/apps/Messaging/+/master/src/com/android/messaging/sms/MmsSmsUtils.java
 */
object MessageUtils {

    val NAME_ADDR_EMAIL_PATTERN = Pattern.compile("\\s*(\"[^\"]*\"|[^<>\"]+)\\s*<([^<>]+)>\\s*")

    fun extractAddrSpec(address: String): String {
        val match = NAME_ADDR_EMAIL_PATTERN.matcher(address)
        return if (match.matches()) {
            match.group(2)
        } else address
    }

    fun isEmailAddress(address: String): Boolean {
        if (TextUtils.isEmpty(address)) {
            return false
        }
        val s = extractAddrSpec(address)
        val match = Patterns.EMAIL_ADDRESS.matcher(s)
        return match.matches()
    }

}