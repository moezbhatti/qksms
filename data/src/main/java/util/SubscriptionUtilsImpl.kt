package util

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import io.reactivex.Observable
import javax.inject.Inject

class SubscriptionUtilsImpl @Inject constructor(context: Context) : SubscriptionUtils {

    private val subscriptionManager = SubscriptionManager.from(context)

    override val subscriptions: List<SubscriptionInfo>
        get() = subscriptionManager.activeSubscriptionInfoList ?: listOf()

    override val subscriptionsObservable: Observable<List<SubscriptionInfo>> = ActiveSubscriptionObservable(subscriptionManager)

    override fun getSubIdForAddress(address: String): Int {
        return subscriptions.firstOrNull { PhoneNumberUtils.compare(it.number, address) }?.subscriptionId ?: -1
    }

}