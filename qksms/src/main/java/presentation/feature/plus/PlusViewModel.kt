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
package presentation.feature.plus

import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.BillingManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import presentation.common.base.QkViewModel
import javax.inject.Inject

class PlusViewModel : QkViewModel<PlusView, PlusState>(PlusState()) {

    @Inject lateinit var billingManager: BillingManager

    init {
        appComponent.inject(this)

        disposables += billingManager.plusStatus
                .subscribe { plan ->
                    newState { it.copy(currentPlan = plan) }
                }

        disposables += billingManager.subscriptions
                .subscribe { subs ->
                    newState {
                        it.copy(
                                supporterPrice = subs.firstOrNull { it.sku == BillingManager.SKU_3 }?.price ?: "",
                                donorPrice = subs.firstOrNull { it.sku == BillingManager.SKU_5 }?.price ?: "",
                                philanthropistPrice = subs.firstOrNull { it.sku == BillingManager.SKU_10 }?.price ?: "")
                    }
                }
    }

    override fun bindView(view: PlusView) {
        super.bindView(view)

        Observable.merge(
                view.supporterSelectedIntent.map { BillingManager.SKU_3 },
                view.donorSelectedIntent.map { BillingManager.SKU_5 },
                view.philanthropistSelectedIntent.map { BillingManager.SKU_10 })
                .autoDisposable(view.scope())
                .subscribe { sku -> view.initiatePurchaseFlow(billingManager, sku) }
    }

}