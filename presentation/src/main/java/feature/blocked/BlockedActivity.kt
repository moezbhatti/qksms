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
package presentation.feature.blocked

import android.app.AlertDialog
import android.os.Bundle
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import injection.appComponent
import common.util.extensions.setVisible
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.blocked_activity.*
import presentation.common.base.QkThemedActivity
import javax.inject.Inject

class BlockedActivity : QkThemedActivity<BlockedViewModel>(), BlockedView {

    @Inject lateinit var blockedAdapter: BlockedAdapter

    override val viewModelClass = BlockedViewModel::class
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

        conversations.adapter = blockedAdapter
    }

    override fun render(state: BlockedState) {
        blockedAdapter.flowable = state.data
        empty.setVisible(state.empty)
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