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

import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.BillingManager
import common.util.extensions.setBackgroundTint
import common.util.extensions.setTint
import common.util.extensions.setVisible
import kotlinx.android.synthetic.main.qksms_plus_activity.*
import presentation.common.base.QkActivity
import presentation.common.widget.QkTextView

class PlusActivity : QkActivity<PlusViewModel>(), PlusView {

    override val viewModelClass = PlusViewModel::class
    override val supporterSelectedIntent by lazy { supporter.clicks() }
    override val donorSelectedIntent by lazy { donor.clicks() }
    override val philanthropistSelectedIntent by lazy { philanthropist.clicks() }

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qksms_plus_activity)
        setTitle(R.string.title_qksms_plus)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        colors.textPrimary
                .autoDisposable(scope())
                .subscribe { color ->
                    collapsingToolbar.setCollapsedTitleTextColor(color)
                    collapsingToolbar.setExpandedTitleColor(color)
                }

        colors.background
                .autoDisposable(scope())
                .subscribe { color -> window.decorView.setBackgroundColor(color) }

        colors.separator
                .autoDisposable(scope())
                .subscribe { color -> thanks.setBackgroundTint(color) }

        colors.theme
                .autoDisposable(scope())
                .subscribe { color ->
                    thanksIcon.setTint(color)
                    supporter.setBackgroundTint(color)
                    donor.setBackgroundTint(color)
                    philanthropist.setBackgroundTint(color)
                }
    }

    override fun render(state: PlusState) {
        description.text = getString(R.string.qksms_plus_description_summary, state.supporterPrice)

        thanks.setVisible(state.currentPlan != BillingManager.UpgradeStatus.REGULAR)

        val supportedSelected = state.currentPlan == BillingManager.UpgradeStatus.SUPPORTER
        supporter.isSelected = supportedSelected
        supporterPrice.text = state.supporterPrice
        supporterName.setColor(getPrimaryTextColorAttr(supportedSelected))
        supporterPrice.setColor(getPrimaryTextColorAttr(supportedSelected))
        supporterPeriod.setColor(getSecondaryTextColorAttr(supportedSelected))

        val donorSelected = state.currentPlan == BillingManager.UpgradeStatus.DONOR
        donor.isSelected = donorSelected
        donorPrice.text = state.donorPrice
        donorName.setColor(getPrimaryTextColorAttr(donorSelected))
        donorPrice.setColor(getPrimaryTextColorAttr(donorSelected))
        donorPeriod.setColor(getSecondaryTextColorAttr(donorSelected))

        val philanthropistSelected = state.currentPlan == BillingManager.UpgradeStatus.PHILANTHROPIST
        philanthropist.isSelected = philanthropistSelected
        philanthropistPrice.text = state.philanthropistPrice
        philanthropistName.setColor(getPrimaryTextColorAttr(philanthropistSelected))
        philanthropistPrice.setColor(getPrimaryTextColorAttr(philanthropistSelected))
        philanthropistPeriod.setColor(getSecondaryTextColorAttr(philanthropistSelected))
    }

    private fun getPrimaryTextColorAttr(selected: Boolean): Int {
        return if (selected) QkTextView.COLOR_PRIMARY_ON_THEME else QkTextView.COLOR_PRIMARY
    }

    private fun getSecondaryTextColorAttr(selected: Boolean): Int {
        return if (selected) QkTextView.COLOR_SECONDARY_ON_THEME else QkTextView.COLOR_SECONDARY
    }

    override fun initiatePurchaseFlow(billingManager: BillingManager, sku: String) {
        billingManager.initiatePurchaseFlow(this, sku)
    }

}