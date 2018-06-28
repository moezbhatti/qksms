package com.klinker.android.send_message

import android.os.Build
import android.telephony.SmsManager

object SmsManagerFactory {

    fun createSmsManager(subscriptionId: Int): SmsManager {
        var manager: SmsManager? = null

        if (subscriptionId != -1 && Build.VERSION.SDK_INT >= 22) {
            try {
                manager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return manager ?: SmsManager.getDefault()
    }
}
