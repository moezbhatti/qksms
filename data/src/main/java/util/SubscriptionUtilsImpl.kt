package util

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import manager.PermissionManager
import javax.inject.Inject

class SubscriptionUtilsImpl @Inject constructor(
        context: Context,
        private val permissions: PermissionManager
) : SubscriptionUtils {

    /**
     * If we don't have the phone permission, then return a null [SubscriptionManager]
     */
    private val subscriptionManager: SubscriptionManager? = SubscriptionManager.from(context)
        get() = field?.takeIf { permissions.hasPhone() }

    override val subscriptions: List<SubscriptionInfo>
        get() = subscriptionManager?.activeSubscriptionInfoList ?: listOf()

    override val subscriptionsObservable: Observable<List<SubscriptionInfo>> = ActiveSubscriptionObservable(subscriptionManager)

    override fun getSubIdForAddress(address: String): Int {
        return subscriptions.firstOrNull { PhoneNumberUtils.compare(it.number, address) }?.subscriptionId ?: -1
    }

}

class ActiveSubscriptionObservable(
        private val subscriptionManager: SubscriptionManager?
) : Observable<List<SubscriptionInfo>>() {

    override fun subscribeActual(observer: Observer<in List<SubscriptionInfo>>) {
        observer.onNext(subscriptionManager?.activeSubscriptionInfoList ?: listOf())

        val listener = Listener(subscriptionManager, observer)
        observer.onSubscribe(listener)
        subscriptionManager?.addOnSubscriptionsChangedListener(listener)
    }

    internal class Listener(
            private val subscriptionManager: SubscriptionManager?,
            private val observer: Observer<in List<SubscriptionInfo>>
    ) : Disposable, SubscriptionManager.OnSubscriptionsChangedListener() {

        private var disposed: Boolean = false

        override fun onSubscriptionsChanged() {
            if (!isDisposed) {
                observer.onNext(subscriptionManager?.activeSubscriptionInfoList ?: listOf())
            }
        }

        override fun isDisposed(): Boolean = disposed

        override fun dispose() {
            disposed = true
            subscriptionManager?.removeOnSubscriptionsChangedListener(this)
        }

    }

}