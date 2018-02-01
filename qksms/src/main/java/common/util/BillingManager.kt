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

    enum class UpgradeStatus { REGULAR, LEGACY, SUPPORTER, DONOR, PHILANTHROPIST }

    companion object {
        const val SKU_LEGACY = "remove_ads"
        const val SKU_3 = "qksms_plus_3"
        const val SKU_5 = "qksms_plus_5"
        const val SKU_10 = "qksms_plus_10"
    }

    val subscriptions: Observable<List<SkuDetails>> = BehaviorSubject.create()
    val plusStatus: Observable<UpgradeStatus>

    private val subSkus = listOf(SKU_3, SKU_5, SKU_10)
    private val purchaseList = mutableListOf<Purchase>()
    private val purchaseListObservable: Observable<List<Purchase>> = BehaviorSubject.create()

    private val billingClient: BillingClient = BillingClient.newBuilder(context).setListener(this).build()
    private var isServiceConnected = false

    init {
        startServiceConnection {
            queryPurchases()
            querySkuDetailsAsync()
        }

        plusStatus = purchaseListObservable
                .map { purchases ->
                    when {
                        purchases.any { it.sku == SKU_10 } -> UpgradeStatus.PHILANTHROPIST
                        purchases.any { it.sku == SKU_5 } -> UpgradeStatus.DONOR
                        purchases.any { it.sku == SKU_3 } -> UpgradeStatus.SUPPORTER
                        purchases.any { it.sku == SKU_LEGACY } -> UpgradeStatus.LEGACY
                        else -> UpgradeStatus.PHILANTHROPIST
                    }
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
            (this.purchaseListObservable as Subject).onNext(purchaseList)
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
            val subParams = SkuDetailsParams.newBuilder().setSkusList(subSkus).setType(BillingClient.SkuType.SUBS)
            billingClient.querySkuDetailsAsync(subParams.build()) { responseCode, skuDetailsList ->
                if (responseCode == BillingResponse.OK) {
                    (subscriptions as Subject).onNext(skuDetailsList)
                }
            }
        }
    }

    fun initiatePurchaseFlow(activity: Activity, sku: String) {
        executeServiceRequest {
            val oldSkus = purchaseList
                    .filter { product -> subSkus.contains(product.sku) }
                    .map { product -> product.sku }

            val params = BillingFlowParams.newBuilder().setSku(sku).setType(SkuType.SUBS).setOldSkus(ArrayList(oldSkus))
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
                purchaseList.clear()
                purchases?.let { purchaseList.addAll(it) }
                (this.purchaseListObservable as Subject).onNext(purchaseList)
            }

            else -> {
                // Ignored
            }
        }
    }

}