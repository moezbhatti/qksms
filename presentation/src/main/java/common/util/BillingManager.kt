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
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponse
import com.android.billingclient.api.BillingClient.SkuType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class BillingManager @Inject constructor(context: Context) : PurchasesUpdatedListener {

    enum class UpgradeStatus { REGULAR, UPGRADED }

    companion object {
        const val SKU_PLUS = "remove_ads"
        const val SKU_PLUS_DONATE = "qksms_plus_donate"
    }

    val products: Observable<List<SkuDetails>> = BehaviorSubject.create()
    val plusStatus: Observable<UpgradeStatus>

    private val skus = listOf(SKU_PLUS, SKU_PLUS_DONATE)
    private val purchaseList = mutableListOf<Purchase>()
    private val purchaseListObservable: Observable<List<Purchase>> = BehaviorSubject.createDefault(listOf())

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
                        purchases.any { it.sku == SKU_PLUS } -> UpgradeStatus.UPGRADED
                        purchases.any { it.sku == SKU_PLUS_DONATE } -> UpgradeStatus.UPGRADED
                        else -> UpgradeStatus.REGULAR
                    }
                }
    }

    private fun queryPurchases() {
        executeServiceRequest {
            val purchasesResult = billingClient.queryPurchases(SkuType.INAPP)

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
            val subParams = SkuDetailsParams.newBuilder().setSkusList(skus).setType(BillingClient.SkuType.INAPP)
            billingClient.querySkuDetailsAsync(subParams.build()) { responseCode, skuDetailsList ->
                if (responseCode == BillingResponse.OK) {
                    (products as Subject).onNext(skuDetailsList)
                }
            }
        }
    }

    fun initiatePurchaseFlow(activity: Activity, sku: String) {
        executeServiceRequest {
            val params = BillingFlowParams.newBuilder().setSku(sku).setType(SkuType.INAPP)
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