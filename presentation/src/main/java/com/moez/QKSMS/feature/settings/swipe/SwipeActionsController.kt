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
package com.moez.QKSMS.feature.settings.swipe

import android.view.View
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkDialog
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.animateLayoutChanges
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.injection.appComponent
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.swipe_actions_controller.*
import javax.inject.Inject

class SwipeActionsController : QkController<SwipeActionsView, SwipeActionsState, SwipeActionsPresenter>(), SwipeActionsView {

    @Inject override lateinit var presenter: SwipeActionsPresenter
    @Inject lateinit var actionsDialog: QkDialog
    @Inject lateinit var colors: Colors

    /**
     * Allows us to subscribe to [actionClicks] more than once
     */
    private val actionClicks: Subject<SwipeActionsView.Action> = PublishSubject.create()

    init {
        appComponent.inject(this)
        layoutRes = R.layout.swipe_actions_controller

        actionsDialog.adapter.setData(R.array.settings_swipe_actions)
    }

    override fun onViewCreated() {
        colors.theme().let { theme ->
            rightIcon.setBackgroundTint(theme.theme)
            rightIcon.setTint(theme.textPrimary)
            leftIcon.setBackgroundTint(theme.theme)
            leftIcon.setTint(theme.textPrimary)
        }

        right.postDelayed({ right?.animateLayoutChanges = true }, 100)
        left.postDelayed({ left?.animateLayoutChanges = true }, 100)

        Observable.merge(
                right.clicks().map { SwipeActionsView.Action.RIGHT },
                left.clicks().map { SwipeActionsView.Action.LEFT })
                .autoDisposable(scope())
                .subscribe(actionClicks)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.settings_swipe_actions)
        showBackButton(true)
    }

    override fun actionClicks(): Observable<SwipeActionsView.Action> = actionClicks

    override fun actionSelected(): Observable<Int> = actionsDialog.adapter.menuItemClicks

    override fun showSwipeActions(selected: Int) {
        actionsDialog.adapter.selectedItem = selected
        activity?.let(actionsDialog::show)
    }

    override fun render(state: SwipeActionsState) {
        rightIcon.isVisible = state.rightIcon != 0
        rightIcon.setImageResource(state.rightIcon)
        rightLabel.text = state.rightLabel

        leftIcon.isVisible = state.leftIcon != 0
        leftIcon.setImageResource(state.leftIcon)
        leftLabel.text = state.leftLabel
    }

}