/*
 * Copyright (C) 2020 Moez Bhatti <moez.bhatti@gmail.com>
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

package com.moez.QKSMS.common.util

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchaseHistory
import com.android.billingclient.api.queryPurchasesAsync
import com.moez.QKSMS.manager.AnalyticsManager
import com.moez.QKSMS.manager.BillingManager
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManagerImpl @Inject constructor(
        context: Context,
        private val analyticsManager: AnalyticsManager
) : BillingManager, BillingClientStateListener, PurchasesUpdatedListener {

    private val productsSubject: Subject<List<ProductDetails>> = BehaviorSubject.create()
    override val products: Observable<List<BillingManager.Product>> = productsSubject
            .map { productDetailsList ->
                productDetailsList.map { productDetails ->
                    BillingManager.Product(
                            sku = productDetails.productId,
                            price = productDetails.oneTimePurchaseOfferDetails!!.formattedPrice,
                            priceCurrencyCode = productDetails.oneTimePurchaseOfferDetails!!.priceCurrencyCode
                    )
                }
            }

    private val purchaseListSubject = BehaviorSubject.create<List<Purchase>>()
    override val upgradeStatus: Observable<Boolean> = purchaseListSubject
            .map { purchases ->
                purchases
                        .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                        .flatMap { it.products }
                        .any { it in skus }
            }
            .distinctUntilChanged()
            .doOnNext { upgraded -> analyticsManager.setUserProperty("Upgraded", upgraded) }

    private val skus = listOf(BillingManager.SKU_PLUS, BillingManager.SKU_PLUS_DONATE)
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

    private val billingClientState = MutableSharedFlow<Int>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        billingClientState.tryEmit(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
    }

    override suspend fun checkForPurchases() = executeServiceRequest {
        // Query from the local cache first
        queryPurchases()

        // Update the cache, then read from it again
        queryPurchaseHistory()
        queryPurchases()
    }

    override suspend fun queryProducts() = executeServiceRequest {
        val productList = skus.map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(sku)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)

        val result = billingClient.queryProductDetails(params.build())
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            productsSubject.onNext(result.productDetailsList.orEmpty())
        } else {
            Timber.w("Error querying products", result.billingResult)
        }
    }

    override suspend fun initiatePurchaseFlow(activity: Activity, sku: String) = executeServiceRequest {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(sku)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()
                ))
                .build()

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(queryProductDetailsParams)
        }

        if (productDetailsResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Timber.w("Error querying product details", productDetailsResult.billingResult)
            return@executeServiceRequest
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetailsResult.productDetailsList!!.first())
                .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            GlobalScope.launch(Dispatchers.IO) {
                handlePurchases(purchases.orEmpty())
            }
        } else {
            Timber.w("Error purchasing", result)
        }
    }

    /**
     * Queries the local purchases saved in Google Play's cache
     */
    private suspend fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

        val result = billingClient.queryPurchasesAsync(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            handlePurchases(result.purchasesList)
        } else {
            Timber.w("Error checking for purchases", result.billingResult)
        }
    }

    /**
     * Fetches the list of purchases and updates the local cache
     */
    private suspend fun queryPurchaseHistory() {
        val params = QueryPurchaseHistoryParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

        val result = billingClient.queryPurchaseHistory(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Timber.w("Error querying purchase history", result.billingResult)
            return
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>) = executeServiceRequest {
        purchases.forEach { purchase ->
            if (!purchase.isAcknowledged && purchase.purchaseState == PurchaseState.PURCHASED) {
                val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                Timber.i("Acknowledging purchase ${purchase.orderId}")
                val result = billingClient.acknowledgePurchase(params)
                Timber.i("Acknowledgement result: ${result.responseCode}, ${result.debugMessage}")
            }
        }

        purchaseListSubject.onNext(purchases)
    }

    private suspend fun executeServiceRequest(runnable: suspend () -> Unit) {
        if (billingClientState.first() != BillingClient.BillingResponseCode.OK) {
            Timber.i("Starting billing service")
            billingClient.startConnection(this)
        }

        billingClientState.first { state -> state == BillingClient.BillingResponseCode.OK }
        runnable()
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        Timber.i("Billing response: ${result.responseCode}")
        billingClientState.tryEmit(result.responseCode)
    }

    override fun onBillingServiceDisconnected() {
        Timber.i("Billing service disconnected")
        billingClientState.tryEmit(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
    }

}
