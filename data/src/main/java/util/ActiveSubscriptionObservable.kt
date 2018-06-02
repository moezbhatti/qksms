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

import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class ActiveSubscriptionObservable(private val subscriptionManager: SubscriptionManager) : Observable<List<SubscriptionInfo>>() {

    override fun subscribeActual(observer: Observer<in List<SubscriptionInfo>>) {
        observer.onNext(subscriptionManager.activeSubscriptionInfoList ?: listOf())

        val listener = Listener(subscriptionManager, observer)
        observer.onSubscribe(listener)
        subscriptionManager.addOnSubscriptionsChangedListener(listener)
    }

    internal class Listener(
            private val subscriptionManager: SubscriptionManager,
            private val observer: Observer<in List<SubscriptionInfo>>
    ) : Disposable, SubscriptionManager.OnSubscriptionsChangedListener() {

        private var disposed: Boolean = false

        override fun onSubscriptionsChanged() {
            if (!isDisposed) {
                observer.onNext(subscriptionManager.activeSubscriptionInfoList ?: listOf())
            }
        }

        override fun isDisposed(): Boolean = disposed

        override fun dispose() {
            disposed = true
            subscriptionManager.removeOnSubscriptionsChangedListener(this)
        }

    }

}