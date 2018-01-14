package com.klinker.android.send_message

import android.os.Build
import android.telephony.SmsManager

object SmsManagerFactory {

    fun createSmsManager(settings: Settings) = createSmsManager(settings.subscriptionId)

    fun createSmsManager(subscriptionId: Int): SmsManager {
        var manager: SmsManager? = null

        if (subscriptionId != Settings.DEFAULT_SUBSCRIPTION_ID && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                manager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return manager ?: SmsManager.getDefault()
    }
}
