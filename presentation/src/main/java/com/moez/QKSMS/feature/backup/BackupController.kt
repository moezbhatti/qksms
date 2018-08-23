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
package com.moez.QKSMS.feature.backup

import android.app.AlertDialog
import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.model.Backup
import io.reactivex.Observable
import kotlinx.android.synthetic.main.backup_controller.*
import kotlinx.android.synthetic.main.backup_list_dialog.view.*
import javax.inject.Inject

class BackupController : QkController<BackupView, BackupState, BackupPresenter>(), BackupView {

    @Inject lateinit var adapter: BackupAdapter
    @Inject override lateinit var presenter: BackupPresenter

    private val dialog by lazy {
        val view = View.inflate(activity, R.layout.backup_list_dialog, null)
                .apply { files.adapter = adapter.apply { emptyView = empty } }

        AlertDialog.Builder(activity)
                .setView(view)
                .setCancelable(true)
                .create()
    }

    init {
        appComponent.inject(this)
        layoutRes = R.layout.backup_controller
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.backup_title)
        showBackButton(true)
    }

    override fun onViewCreated() {
        super.onViewCreated()

        themedActivity?.colors?.theme()?.let { theme ->
            fab.setBackgroundTint(theme.theme)
            fabIcon.setTint(theme.textPrimary)
            fabLabel.setTextColor(theme.textPrimary)
        }
    }

    override fun render(state: BackupState) {
        restore.isEnabled = state.upgraded

        adapter.data = state.backups

        fabIcon.setImageResource(when (state.upgraded) {
            true -> R.drawable.ic_file_upload_black_24dp
            false -> R.drawable.ic_star_black_24dp
        })

        fabLabel.setText(when (state.upgraded) {
            true -> R.string.backup_now
            false -> R.string.title_qksms_plus
        })
    }

    override fun restoreClicks(): Observable<*> = restore.clicks()

    override fun restoreFileSelected(): Observable<Backup> = adapter.backupSelected

    override fun fabClicks(): Observable<*> = fab.clicks()

    override fun selectFile() = dialog.show()

}