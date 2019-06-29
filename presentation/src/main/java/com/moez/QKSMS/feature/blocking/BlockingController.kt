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
package com.moez.QKSMS.feature.blocking

import android.view.View
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.RouterTransaction
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkChangeHandler
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.animateLayoutChanges
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.feature.blocking.manager.BlockingManagerController
import com.moez.QKSMS.feature.blocking.numbers.BlockedNumbersController
import com.moez.QKSMS.injection.appComponent
import kotlinx.android.synthetic.main.blocking_controller.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import javax.inject.Inject

class BlockingController : QkController<BlockingView, BlockingState, BlockingPresenter>(), BlockingView {

    override val blockingManagerIntent by lazy { blockingManager.clicks() }
    override val settingsClicks by lazy { settings.clicks() }
    override val dropClickedIntent by lazy { drop.clicks() }
    override val conversationClicks by lazy { blockingAdapter.clicks }

    @Inject lateinit var blockingAdapter: BlockingAdapter
    @Inject lateinit var colors: Colors
    @Inject override lateinit var presenter: BlockingPresenter

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.blocking_controller
    }

    override fun onViewCreated() {
        super.onViewCreated()

        settings.setTint(colors.theme().theme)
        blockingAdapter.emptyView = empty
        conversations.adapter = blockingAdapter

        parent.postDelayed({ parent?.animateLayoutChanges = true }, 100)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.blocking_title)
        showBackButton(true)
    }

    override fun render(state: BlockingState) {
        blockingManager.summary = state.blockingManager
        drop.checkbox.isChecked = state.dropEnabled
        blockedLabel.isVisible = state.data?.isNotEmpty() == true
        blockingAdapter.updateData(state.data)
    }

    override fun openBlockingManager() {
        router.pushController(RouterTransaction.with(BlockingManagerController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

    override fun openBlockedNumbers() {
        router.pushController(RouterTransaction.with(BlockedNumbersController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

}
