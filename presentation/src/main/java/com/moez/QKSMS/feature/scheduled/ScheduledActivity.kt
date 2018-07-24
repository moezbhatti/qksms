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
package com.moez.QKSMS.feature.scheduled

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkDialog
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.util.FontProvider
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.collapsing_toolbar.*
import kotlinx.android.synthetic.main.scheduled_activity.*
import javax.inject.Inject


class ScheduledActivity : QkThemedActivity(), ScheduledView {

    @Inject lateinit var dialog: QkDialog
    @Inject lateinit var fontProvider: FontProvider
    @Inject lateinit var messageAdapter: ScheduledMessageAdapter
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val messageClickIntent by lazy { messageAdapter.clicks }
    override val messageMenuIntent by lazy { dialog.adapter.menuItemClicks }
    override val composeIntent by lazy { compose.clicks() }
    override val upgradeIntent by lazy { upgrade.clicks() }

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[ScheduledViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scheduled_activity)
        setTitle(R.string.scheduled_title)
        showBackButton(true)
        viewModel.bindView(this)

        if (!prefs.systemFont.get()) {
            fontProvider.getLato { lato ->
                val typeface = Typeface.create(lato, Typeface.BOLD)
                collapsingToolbar.setCollapsedTitleTypeface(typeface)
                collapsingToolbar.setExpandedTitleTypeface(typeface)
            }
        }

        dialog.title = getString(R.string.scheduled_options_title)
        dialog.adapter.setData(R.array.scheduled_options)

        messageAdapter.emptyView = empty
        messages.adapter = messageAdapter

        colors.theme().let { theme ->
            sampleMessage.setBackgroundTint(theme.theme)
            sampleMessage.setTextColor(theme.textPrimary)
            compose.setTint(theme.textPrimary)
            compose.setBackgroundTint(theme.theme)
            upgrade.setBackgroundTint(theme.theme)
            upgradeIcon.setTint(theme.textPrimary)
            upgradeLabel.setTextColor(theme.textPrimary)
        }
    }

    override fun render(state: ScheduledState) {
        messageAdapter.updateData(state.scheduledMessages)

        compose.isVisible = state.upgraded
        upgrade.isVisible = !state.upgraded
    }

    override fun showMessageOptions() {
        dialog.show(this)
    }

}