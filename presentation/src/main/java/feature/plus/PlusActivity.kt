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
package feature.plus

import android.graphics.Typeface
import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.base.QkThemedActivity
import common.util.BillingManager
import common.util.FontProvider
import common.util.extensions.setBackgroundTint
import common.util.extensions.setTint
import common.util.extensions.setVisible
import injection.appComponent
import kotlinx.android.synthetic.main.qksms_plus_activity.*
import javax.inject.Inject

class PlusActivity : QkThemedActivity<PlusViewModel>(), PlusView {

    @Inject lateinit var fontProvider: FontProvider

    override val viewModelClass = PlusViewModel::class
    override val upgradeIntent by lazy { upgrade.clicks() }
    override val upgradeDonateIntent by lazy { upgradeDonate.clicks() }

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qksms_plus_activity)
        setTitle(R.string.title_qksms_plus)
        showBackButton(true)
        viewModel.bindView(this)

        free.setVisible(false)

        fontProvider.getLato { lato ->
            val typeface = Typeface.create(lato, Typeface.BOLD)
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
                    upgraded.setBackgroundTint(color)
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

        toUpgrade.setVisible(!state.upgraded)
        upgraded.setVisible(state.upgraded)
    }

    override fun initiatePurchaseFlow(billingManager: BillingManager, sku: String) {
        billingManager.initiatePurchaseFlow(this, sku)
    }

}