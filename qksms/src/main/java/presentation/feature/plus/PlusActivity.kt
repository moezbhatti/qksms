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

import android.graphics.Typeface
import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.BillingManager
import common.util.FontProvider
import common.util.extensions.setBackgroundTint
import common.util.extensions.setTint
import common.util.extensions.setVisible
import kotlinx.android.synthetic.main.qksms_plus_activity.*
import presentation.common.base.QkActivity
import javax.inject.Inject

class PlusActivity : QkActivity<PlusViewModel>(), PlusView {

    @Inject lateinit var fontProvider: FontProvider

    override val viewModelClass = PlusViewModel::class
    override val upgradeIntent by lazy { upgrade.clicks() }
    override val upgradeDonateIntent by lazy { upgradeDonate.clicks() }

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qksms_plus_activity)
        setTitle(R.string.title_qksms_plus)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        fontProvider.getLato {
            val typeface = Typeface.create(it, Typeface.BOLD)
            collapsingToolbar.setCollapsedTitleTypeface(typeface)
            collapsingToolbar.setExpandedTitleTypeface(typeface)
        }

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
                .subscribe { color ->
                    upgradeDonate.setBackgroundTint(color)
                    thanks.setBackgroundTint(color)
                }

        colors.theme
                .autoDisposable(scope())
                .subscribe { color ->
                    upgrade.setBackgroundTint(color)
                    thanksIcon.setTint(color)
                }
    }

    override fun render(state: PlusState) {
        description.text = getString(R.string.qksms_plus_description_summary, state.upgradePrice)
        upgrade.text = getString(R.string.qksms_plus_upgrade, state.upgradePrice, state.currency)
        upgradeDonate.text = getString(R.string.qksms_plus_upgrade_donate, state.upgradeDonatePrice, state.currency)

        upgrade.setVisible(state.currentPlan == BillingManager.UpgradeStatus.REGULAR)
        upgradeDonate.setVisible(state.currentPlan == BillingManager.UpgradeStatus.REGULAR)
        thanks.setVisible(state.currentPlan != BillingManager.UpgradeStatus.REGULAR)
    }

    override fun initiatePurchaseFlow(billingManager: BillingManager, sku: String) {
        billingManager.initiatePurchaseFlow(this, sku)
    }

}