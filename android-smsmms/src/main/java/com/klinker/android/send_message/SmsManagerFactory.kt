package com.klinker.android.send_message

import android.telephony.SmsManager

object SmsManagerFactory {

    fun createSmsManager(subscriptionId: Int): SmsManager {
        var manager: SmsManager? = null

        if (subscriptionId != -1) {
            try {
                manager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return manager ?: SmsManager.getDefault()
    }
}
