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

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.util.QkActivityResultContracts
import com.moez.QKSMS.common.util.extensions.getLabel
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setNegativeButton
import com.moez.QKSMS.common.util.extensions.setPositiveButton
import com.moez.QKSMS.common.util.extensions.setShowing
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.common.widget.PreferenceView
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.repository.BackupRepository
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.backup_controller.*
import kotlinx.android.synthetic.main.preference_view.view.*
import javax.inject.Inject

class BackupController : QkController<BackupView, BackupState, BackupPresenter>(), BackupView {

    @Inject override lateinit var presenter: BackupPresenter

    private val selectFolderCancelSubject: Subject<Unit> = PublishSubject.create()
    private val selectFolderConfirmSubject: Subject<Unit> = PublishSubject.create()

    private val restoreErrorConfirmSubject: Subject<Unit> = PublishSubject.create()

    private val confirmRestoreCancelSubject: Subject<Unit> = PublishSubject.create()
    private val confirmRestoreConfirmSubject: Subject<Unit> = PublishSubject.create()

    private val stopRestoreConfirmSubject: Subject<Unit> = PublishSubject.create()
    private val stopRestoreCancelSubject: Subject<Unit> = PublishSubject.create()

    private val documentTreeSelectedSubject: Subject<Uri> = PublishSubject.create()
    private val documentSelectedSubject: Subject<Uri> = PublishSubject.create()

    private val stopRestoreDialog by lazy {
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.backup_restore_stop_title)
                .setMessage(R.string.backup_restore_stop_message)
                .setPositiveButton(R.string.button_stop, stopRestoreConfirmSubject)
                .setNegativeButton(R.string.button_cancel, stopRestoreCancelSubject)
                .setCancelable(false)
                .create()
    }

    private val selectLocationRationaleDialog by lazy {
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.backup_select_location_rationale_title)
                .setMessage(R.string.backup_select_location_rationale_message)
                .setPositiveButton(R.string.button_continue, selectFolderConfirmSubject)
                .setNegativeButton(R.string.button_cancel, selectFolderCancelSubject)
                .setCancelable(false)
                .create()
    }

    private val selectedBackupErrorDialog by lazy {
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.backup_selected_backup_error_title)
                .setMessage(R.string.backup_selected_backup_error_message)
                .setPositiveButton(R.string.button_continue, restoreErrorConfirmSubject)
                .setCancelable(false)
                .create()
    }

    private val selectedBackupDetailsDialog by lazy {
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.backup_selected_backup_details_title)
                .setPositiveButton(R.string.backup_restore_title, confirmRestoreConfirmSubject)
                .setNegativeButton(R.string.button_cancel, confirmRestoreCancelSubject)
                .setCancelable(false)
                .create()
    }

    private lateinit var openDirectory: ActivityResultLauncher<Uri>
    private lateinit var openDocument: ActivityResultLauncher<QkActivityResultContracts.OpenDocumentParams>

    init {
        appComponent.inject(this)
        layoutRes = R.layout.backup_controller
    }

    override fun onContextAvailable(context: Context) {
        // Init activity result contracts
        openDirectory = themedActivity!!
            .registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                uri?.let(documentTreeSelectedSubject::onNext)
            }

        openDocument = themedActivity!!
            .registerForActivityResult(QkActivityResultContracts.OpenDocument()) { uri ->
                uri?.let(documentSelectedSubject::onNext)
            }
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
            progressBar.indeterminateTintList = ColorStateList.valueOf(theme.theme)
            progressBar.progressTintList = ColorStateList.valueOf(theme.theme)
            fab.setBackgroundTint(theme.theme)
            fabIcon.setTint(theme.textPrimary)
            fabLabel.setTextColor(theme.textPrimary)
        }

        // Make the list titles bold
        linearLayout.children
                .mapNotNull { it as? PreferenceView }
                .map { it.titleView }
                .forEach { it.setTypeface(it.typeface, Typeface.BOLD) }
    }

    override fun render(state: BackupState) {
        when {
            state.backupProgress.running -> {
                progressIcon.setImageResource(R.drawable.ic_file_upload_black_24dp)
                progressTitle.setText(R.string.backup_backing_up)
                progressSummary.text = state.backupProgress.getLabel(activity!!)
                progressSummary.isVisible = progressSummary.text.isNotEmpty()
                progressCancel.isVisible = false
                val running = (state.backupProgress as? BackupRepository.Progress.Running)
                progressBar.isVisible = state.backupProgress.indeterminate || running?.max ?: 0 > 0
                progressBar.isIndeterminate = state.backupProgress.indeterminate
                progressBar.max = running?.max ?: 0
                progressBar.progress = running?.count ?: 0
                progress.isVisible = true
                fab.isVisible = false
            }

            state.restoreProgress.running -> {
                progressIcon.setImageResource(R.drawable.ic_file_download_black_24dp)
                progressTitle.setText(R.string.backup_restoring)
                progressSummary.text = state.restoreProgress.getLabel(activity!!)
                progressSummary.isVisible = progressSummary.text.isNotEmpty()
                progressCancel.isVisible = true
                val running = (state.restoreProgress as? BackupRepository.Progress.Running)
                progressBar.isVisible = state.restoreProgress.indeterminate || running?.max ?: 0 > 0
                progressBar.isIndeterminate = state.restoreProgress.indeterminate
                progressBar.max = running?.max ?: 0
                progressBar.progress = running?.count ?: 0
                progress.isVisible = true
                fab.isVisible = false
            }

            else -> {
                progress.isVisible = false
                fab.isVisible = true
            }
        }

        selectLocationRationaleDialog.setShowing(state.showLocationRationale)

        selectedBackupErrorDialog.setShowing(state.showSelectedBackupError)

        selectedBackupDetailsDialog.setMessage(state.selectedBackupDetails)
        selectedBackupDetailsDialog.setShowing(state.selectedBackupDetails != null)

        stopRestoreDialog.setShowing(state.showStopRestoreDialog)

        fabIcon.setImageResource(when (state.upgraded) {
            true -> R.drawable.ic_file_upload_black_24dp
            false -> R.drawable.ic_star_black_24dp
        })

        fabLabel.setText(when (state.upgraded) {
            true -> R.string.backup_now
            false -> R.string.title_qksms_plus
        })
    }

    override fun setBackupLocationClicks(): Observable<*> = location.clicks()

    override fun restoreClicks(): Observable<*> = restore.clicks()

    override fun locationRationaleConfirmClicks(): Observable<*> = selectFolderConfirmSubject

    override fun locationRationaleCancelClicks(): Observable<*> = selectFolderCancelSubject

    override fun selectedBackupErrorClicks(): Observable<*> = restoreErrorConfirmSubject

    override fun confirmRestoreBackupConfirmClicks(): Observable<*> = confirmRestoreConfirmSubject

    override fun confirmRestoreBackupCancelClicks(): Observable<*> = confirmRestoreCancelSubject

    override fun stopRestoreClicks(): Observable<*> = progressCancel.clicks()

    override fun stopRestoreConfirmed(): Observable<*> = stopRestoreConfirmSubject

    override fun stopRestoreCancel(): Observable<*> = stopRestoreCancelSubject

    override fun backupClicks(): Observable<*> = fab.clicks()

    override fun documentTreeSelected(): Observable<Uri> = documentTreeSelectedSubject

    override fun documentSelected(): Observable<Uri> = documentSelectedSubject

    override fun selectFolder(initialUri: Uri) {
        openDirectory.launch(initialUri)
    }

    override fun selectFile(initialUri: Uri) {
        openDocument.launch(QkActivityResultContracts.OpenDocumentParams(
                mimeTypes = listOf("application/json"),
                initialUri = initialUri))
    }

}
