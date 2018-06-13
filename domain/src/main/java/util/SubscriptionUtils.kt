package util

import android.telephony.SubscriptionInfo
import io.reactivex.Observable

interface SubscriptionUtils {

    val subscriptions: List<SubscriptionInfo>

    val subscriptionsObservable: Observable<List<SubscriptionInfo>>

    fun getSubIdForAddress(address: String): Int

}