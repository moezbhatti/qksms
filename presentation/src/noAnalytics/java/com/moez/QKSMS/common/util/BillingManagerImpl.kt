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
import com.moez.QKSMS.manager.BillingManager
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManagerImpl @Inject constructor(
): BillingManager {

    override val products: Observable<List<BillingManager.Product>> = BehaviorSubject.createDefault(listOf())
    override val upgradeStatus: Observable<Boolean> = BehaviorSubject.createDefault(true)

    override suspend fun checkForPurchases() = Unit
    override suspend fun queryProducts() = Unit
    override suspend fun initiatePurchaseFlow(activity: Activity, sku: String) = Unit

}
