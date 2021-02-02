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
package com.moez.QKSMS.feature.plus

import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.base.QkViewModel
import com.moez.QKSMS.manager.AnalyticsManager
import com.moez.QKSMS.manager.BillingManager
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class PlusViewModel @Inject constructor(
    private val analyticsManager: AnalyticsManager,
    private val billingManager: BillingManager,
    private val navigator: Navigator
) : QkViewModel<PlusView, PlusState>(PlusState()) {

    init {
        disposables += billingManager.upgradeStatus
                .subscribe { upgraded -> newState { copy(upgraded = upgraded) } }

        disposables += billingManager.products
                .subscribe { products ->
                    newState {
                        val upgrade = products.firstOrNull { it.sku == BillingManager.SKU_PLUS }
                        val upgradeDonate = products.firstOrNull { it.sku == BillingManager.SKU_PLUS_DONATE }
                        copy(upgradePrice = upgrade?.price ?: "", upgradeDonatePrice = upgradeDonate?.price ?: "",
                                currency = upgrade?.priceCurrencyCode ?: upgradeDonate?.priceCurrencyCode ?: "")
                    }
                }
    }

    override fun bindView(view: PlusView) {
        super.bindView(view)

        Observable.merge(
                view.upgradeIntent.map { BillingManager.SKU_PLUS },
                view.upgradeDonateIntent.map { BillingManager.SKU_PLUS_DONATE })
                .doOnNext { sku -> analyticsManager.track("Clicked Upgrade", Pair("sku", sku)) }
                .autoDisposable(view.scope())
                .subscribe { sku -> view.initiatePurchaseFlow(billingManager, sku) }

        view.donateIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showDonation() }

        view.themeClicks
                .autoDisposable(view.scope())
                .subscribe { navigator.showSettings() }

        view.scheduleClicks
                .autoDisposable(view.scope())
                .subscribe { navigator.showScheduled() }

        view.backupClicks
                .autoDisposable(view.scope())
                .subscribe { navigator.showBackup() }

        view.delayedClicks
                .autoDisposable(view.scope())
                .subscribe { navigator.showSettings() }

        view.nightClicks
                .autoDisposable(view.scope())
                .subscribe { navigator.showSettings() }
    }

}
