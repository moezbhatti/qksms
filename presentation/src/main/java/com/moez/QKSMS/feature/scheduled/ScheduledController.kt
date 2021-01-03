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

package com.moez.QKSMS.feature.scheduled

import android.content.Context
import android.graphics.Typeface
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkDialog
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.FontProvider
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.databinding.ScheduledActivityBinding
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.util.Preferences
import javax.inject.Inject

class ScheduledController : QkController<ScheduledView, ScheduledState, ScheduledPresenter, ScheduledActivityBinding>(
        ScheduledActivityBinding::inflate
), ScheduledView {

    @Inject override lateinit var presenter: ScheduledPresenter

    @Inject lateinit var colors: Colors
    @Inject lateinit var context: Context
    @Inject lateinit var dialog: QkDialog
    @Inject lateinit var fontProvider: FontProvider
    @Inject lateinit var messageAdapter: ScheduledMessageAdapter
    @Inject lateinit var prefs: Preferences

    override val messageClickIntent by lazy { messageAdapter.clicks }
    override val messageMenuIntent by lazy { dialog.adapter.menuItemClicks }
    override val composeIntent by lazy { binding.compose.clicks() }
    override val upgradeIntent by lazy { binding.upgrade.clicks() }

    init {
        appComponent.inject(this)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.scheduled_title)
        showBackButton(true)
    }

    override fun onViewCreated() {
        super.onViewCreated()

        if (!prefs.systemFont.get()) {
            fontProvider.getLato { lato ->
                val typeface = Typeface.create(lato, Typeface.BOLD)
                binding.appBarLayout.collapsingToolbar.setCollapsedTitleTypeface(typeface)
                binding.appBarLayout.collapsingToolbar.setExpandedTitleTypeface(typeface)
            }
        }

        dialog.title = context.getString(R.string.scheduled_options_title)
        dialog.adapter.setData(R.array.scheduled_options)

        messageAdapter.emptyView = binding.empty
        binding.messages.adapter = messageAdapter

        colors.theme().let { theme ->
            binding.sampleMessage.setBackgroundTint(theme.theme)
            binding.sampleMessage.setTextColor(theme.textPrimary)
            binding.compose.setTint(theme.textPrimary)
            binding.compose.setBackgroundTint(theme.theme)
            binding.upgrade.setBackgroundTint(theme.theme)
            binding.upgradeIcon.setTint(theme.textPrimary)
            binding.upgradeLabel.setTextColor(theme.textPrimary)
        }
    }

    override fun render(state: ScheduledState) {
        messageAdapter.updateData(state.scheduledMessages)

        binding.compose.isVisible = state.upgraded
        binding.upgrade.isVisible = !state.upgraded
    }

    override fun showMessageOptions() {
        dialog.show(activity!!)
    }
}
