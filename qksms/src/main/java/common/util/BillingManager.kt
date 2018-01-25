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
package common.util

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponse
import com.android.billingclient.api.BillingClient.SkuType
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class BillingManager @Inject constructor(context: Context) : PurchasesUpdatedListener {

    val purchases: Observable<List<Purchase>> = BehaviorSubject.create()
    val iabs: Observable<List<SkuDetails>> = BehaviorSubject.create()
    val subs: Observable<List<SkuDetails>> = BehaviorSubject.create()

    private val iabSkus = listOf("remove_ads")
    private val subSkus = listOf("qksms_plus_3", "qksms_plus_5", "qksms_plus_10")
    private val purchaseList = mutableListOf<Purchase>()

    private val billingClient: BillingClient = BillingClient.newBuilder(context).setListener(this).build()
    private var isServiceConnected = false

    init {
        startServiceConnection {
            queryPurchases()
            querySkuDetailsAsync()
        }
    }

    private fun queryPurchases() {
        executeServiceRequest {
            val purchasesResult = billingClient.queryPurchases(SkuType.INAPP)

            if (billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS) == BillingResponse.OK) {
                val subscriptionResult = billingClient.queryPurchases(SkuType.SUBS)
                if (subscriptionResult.responseCode == BillingResponse.OK) {
                    purchasesResult.purchasesList.addAll(subscriptionResult.purchasesList)
                }
            }

            // Handle purchase result
            purchaseList.clear()
            purchaseList.addAll(purchasesResult.purchasesList)
            (this.purchases as Subject).onNext(purchaseList)
        }
    }


    private fun startServiceConnection(onSuccess: () -> Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@BillingResponse billingResponseCode: Int) {
                if (billingResponseCode == BillingResponse.OK) {
                    isServiceConnected = true
                    onSuccess()
                }
            }

            override fun onBillingServiceDisconnected() {
                isServiceConnected = false
            }
        })
    }

    private fun querySkuDetailsAsync() {
        executeServiceRequest {
            val iabParams = SkuDetailsParams.newBuilder().setSkusList(iabSkus).setType(BillingClient.SkuType.INAPP)
            billingClient.querySkuDetailsAsync(iabParams.build()) { responseCode, skuDetailsList ->
                if (responseCode == BillingResponse.OK) {
                    (iabs as Subject).onNext(skuDetailsList)
                }
            }

            val subParams = SkuDetailsParams.newBuilder().setSkusList(subSkus).setType(BillingClient.SkuType.SUBS)
            billingClient.querySkuDetailsAsync(subParams.build()) { responseCode, skuDetailsList ->
                if (responseCode == BillingResponse.OK) {
                    (subs as Subject).onNext(skuDetailsList)
                }
            }
        }
    }

    fun initiatePurchaseFlow(activity: Activity) {
        executeServiceRequest {
            val params = BillingFlowParams.newBuilder().setSku("qksms_plus_3").setType(SkuType.SUBS)
            billingClient.launchBillingFlow(activity, params.build())
        }
    }

    private fun executeServiceRequest(runnable: () -> Unit) {
        when (isServiceConnected) {
            true -> runnable()
            false -> startServiceConnection(runnable)
        }
    }

    override fun onPurchasesUpdated(resultCode: Int, purchases: List<Purchase>?) {
        when (resultCode) {
            BillingResponse.OK -> {
                purchases?.forEach { purchaseList.add(it) }
                (this.purchases as Subject).onNext(purchaseList)
            }

            else -> {
                // Ignored
            }
        }
    }

}