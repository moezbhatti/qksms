package com.moez.QKSMS.common.util

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