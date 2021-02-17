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

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.BuildConfig
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.util.FontProvider
import com.moez.QKSMS.common.util.extensions.makeToast
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.common.widget.PreferenceView
import com.moez.QKSMS.feature.plus.experiment.UpgradeButtonExperiment
import com.moez.QKSMS.manager.BillingManager
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.collapsing_toolbar.*
import kotlinx.android.synthetic.main.preference_view.view.*
import kotlinx.android.synthetic.main.qksms_plus_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PlusActivity : QkThemedActivity(), PlusView {

    @Inject lateinit var fontProvider: FontProvider
    @Inject lateinit var upgradeButtonExperiment: UpgradeButtonExperiment
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[PlusViewModel::class.java] }

    override val upgradeIntent by lazy { upgrade.clicks() }
    override val upgradeDonateIntent by lazy { upgradeDonate.clicks() }
    override val donateIntent by lazy { donate.clicks() }
    override val themeClicks by lazy { themes.clicks() }
    override val scheduleClicks by lazy { schedule.clicks() }
    override val backupClicks by lazy { backup.clicks() }
    override val delayedClicks by lazy { delayed.clicks() }
    override val nightClicks by lazy { night.clicks() }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qksms_plus_activity)
        setTitle(R.string.title_qksms_plus)
        showBackButton(true)
        viewModel.bindView(this)

        free.setVisible(false)

        if (!prefs.systemFont.get()) {
            fontProvider.getLato { lato ->
                val typeface = Typeface.create(lato, Typeface.BOLD)
                collapsingToolbar.setCollapsedTitleTypeface(typeface)
                collapsingToolbar.setExpandedTitleTypeface(typeface)
            }
        }

        // Make the list titles bold
        linearLayout.children
                .mapNotNull { it as? PreferenceView }
                .map { it.titleView }
                .forEach { it.setTypeface(it.typeface, Typeface.BOLD) }

        val textPrimary = resolveThemeColor(android.R.attr.textColorPrimary)
        collapsingToolbar.setCollapsedTitleTextColor(textPrimary)
        collapsingToolbar.setExpandedTitleColor(textPrimary)

        val theme = colors.theme().theme
        donate.setBackgroundTint(theme)
        upgrade.setBackgroundTint(theme)
        thanksIcon.setTint(theme)
    }

    override fun render(state: PlusState) {
        description.text = getString(R.string.qksms_plus_description_summary, state.upgradePrice)
        upgrade.text = getString(upgradeButtonExperiment.variant, state.upgradePrice, state.currency)
        upgradeDonate.text = getString(R.string.qksms_plus_upgrade_donate, state.upgradeDonatePrice, state.currency)

        val fdroid = BuildConfig.FLAVOR == "noAnalytics"

        free.setVisible(fdroid)
        toUpgrade.setVisible(!fdroid && !state.upgraded)
        upgraded.setVisible(!fdroid && state.upgraded)

        themes.isEnabled = state.upgraded
        schedule.isEnabled = state.upgraded
        backup.isEnabled = state.upgraded
        delayed.isEnabled = state.upgraded
        night.isEnabled = state.upgraded
    }

    override fun initiatePurchaseFlow(billingManager: BillingManager, sku: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                billingManager.initiatePurchaseFlow(this@PlusActivity, sku)
            } catch (e: Exception) {
                Timber.w(e)
                makeToast(R.string.qksms_plus_error)
            }
        }
    }

}