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
package feature.blocked

import android.app.AlertDialog
import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.base.QkThemedActivity
import injection.appComponent
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.blocked_activity.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import javax.inject.Inject

class BlockedActivity : QkThemedActivity<BlockedViewModel>(), BlockedView {

    @Inject lateinit var blockedAdapter: BlockedAdapter

    override val viewModelClass = BlockedViewModel::class
    override val siaClickedIntent by lazy { shouldIAnswer.clicks() }
    override val unblockIntent by lazy { blockedAdapter.unblock }
    override val confirmUnblockIntent: Subject<Unit> = PublishSubject.create()

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blocked_activity)
        setTitle(R.string.blocked_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        colors.background
                .autoDisposable(scope())
                .subscribe { color -> window.decorView.setBackgroundColor(color) }

        blockedAdapter.emptyView = empty
        conversations.adapter = blockedAdapter
    }

    override fun render(state: BlockedState) {
        shouldIAnswer.checkbox.isChecked = state.siaEnabled

        blockedAdapter.updateData(state.data)
    }

    override fun showUnblockDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.blocked_unblock_dialog_title)
                .setMessage(R.string.blocked_unblock_dialog_message)
                .setPositiveButton(R.string.button_unblock, { _, _ -> confirmUnblockIntent.onNext(Unit) })
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

}