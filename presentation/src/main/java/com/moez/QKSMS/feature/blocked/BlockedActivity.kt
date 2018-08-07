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
package com.moez.QKSMS.feature.blocked

import android.app.AlertDialog
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkThemedActivity
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.blocked_activity.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import javax.inject.Inject

class BlockedActivity : QkThemedActivity(), BlockedView {

    @Inject lateinit var blockedAdapter: BlockedAdapter
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val siaClickedIntent by lazy { shouldIAnswer.clicks() }
    override val unblockIntent by lazy { blockedAdapter.unblock }
    override val confirmUnblockIntent: Subject<Long> = PublishSubject.create()

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[BlockedViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blocked_activity)
        setTitle(R.string.blocked_title)
        showBackButton(true)
        viewModel.bindView(this)

        blockedAdapter.emptyView = empty
        conversations.adapter = blockedAdapter
    }

    override fun render(state: BlockedState) {
        shouldIAnswer.checkbox.isChecked = state.siaEnabled

        blockedAdapter.updateData(state.data)
    }

    override fun showUnblockDialog(threadId: Long) {
        AlertDialog.Builder(this)
                .setTitle(R.string.blocked_unblock_dialog_title)
                .setMessage(R.string.blocked_unblock_dialog_message)
                .setPositiveButton(R.string.button_unblock) { _, _ -> confirmUnblockIntent.onNext(threadId) }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

}