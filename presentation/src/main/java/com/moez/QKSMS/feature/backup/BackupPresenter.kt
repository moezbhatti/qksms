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
import androidx.core.net.toUri
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.base.QkPresenter
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.extensions.makeToast
import com.moez.QKSMS.interactor.PerformBackup
import com.moez.QKSMS.manager.BillingManager
import com.moez.QKSMS.repository.BackupRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BackupPresenter @Inject constructor(
    private val backupRepo: BackupRepository,
    private val billingManager: BillingManager,
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator,
    private val performBackup: PerformBackup
) : QkPresenter<BackupView, BackupState>(BackupState()) {

    init {
        disposables += backupRepo.getBackupProgress()
                .sample(16, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribe { progress -> newState { copy(backupProgress = progress) } }

        disposables += backupRepo.getRestoreProgress()
                .sample(16, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribe { progress -> newState { copy(restoreProgress = progress) } }

        disposables += billingManager.upgradeStatus
                .subscribe { upgraded -> newState { copy(upgraded = upgraded) } }
    }

    override fun bindIntents(view: BackupView) {
        super.bindIntents(view)

        view.setBackupLocationClicks()
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(view.scope())
                .subscribe { view.selectFolder(backupRepo.getBackupPathUriForPicker()) }

        view.restoreClicks()
                .withLatestFrom(
                        backupRepo.getBackupProgress(),
                        backupRepo.getRestoreProgress(),
                        billingManager.upgradeStatus)
                { _, backupProgress, restoreProgress, upgraded ->
                    when {
                        !upgraded -> context.makeToast(R.string.backup_restore_error_plus)
                        backupProgress.running -> context.makeToast(R.string.backup_restore_error_backup)
                        restoreProgress.running -> context.makeToast(R.string.backup_restore_error_restore)
                        else -> view.selectFile(backupRepo.getBackupPathUriForPicker())
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.backupClicks()
                .withLatestFrom(billingManager.upgradeStatus) { _, upgraded -> upgraded }
                .autoDisposable(view.scope())
                .subscribe { upgraded ->
                    when {
                        backupRepo.getBackupDocumentTree() == null -> {
                            newState { copy(showLocationRationale = true) }
                        }
                        !upgraded -> navigator.showQksmsPlusActivity("backup_fab")
                        upgraded -> performBackup.execute(Unit)
                    }
                }

        view.locationRationaleConfirmClicks()
                .doOnNext { newState { copy(showLocationRationale = false) } }
                .autoDisposable(view.scope())
                .subscribe { view.selectFolder(backupRepo.getBackupPathUriForPicker()) }

        view.locationRationaleCancelClicks()
                .doOnNext { newState { copy(showLocationRationale = false) } }
                .autoDisposable(view.scope())
                .subscribe()

        view.selectedBackupErrorClicks()
                .autoDisposable(view.scope())
                .subscribe { newState { copy(showSelectedBackupError = false) } }

        view.confirmRestoreBackupConfirmClicks()
                .doOnNext { newState { copy(selectedBackupDetails = null) } }
                .withLatestFrom(view.documentSelected()) { _, backup -> backup }
                .autoDisposable(view.scope())
                .subscribe { backup -> RestoreBackupService.start(context, backup) }

        view.confirmRestoreBackupCancelClicks()
                .doOnNext { newState { copy(selectedBackupDetails = null) } }
                .autoDisposable(view.scope())
                .subscribe()

        view.stopRestoreClicks()
                .autoDisposable(view.scope())
                .subscribe { newState { copy(showStopRestoreDialog = true) } }

        view.stopRestoreConfirmed()
                .doOnNext { newState { copy(showStopRestoreDialog = false) } }
                .autoDisposable(view.scope())
                .subscribe { backupRepo.stopRestore() }

        view.stopRestoreCancel()
                .autoDisposable(view.scope())
                .subscribe { newState { copy(showStopRestoreDialog = false) } }

        view.documentTreeSelected()
                .autoDisposable(view.scope())
                .subscribe { uri -> backupRepo.persistBackupDirectory(uri) }

        view.documentSelected()
                .observeOn(Schedulers.io())
                .autoDisposable(view.scope())
                .subscribe { uri ->
                    try {
                        val backupFile = backupRepo.parseBackup(uri)
                        val date = dateFormatter.getDetailedTimestamp(backupFile.date)
                        val details = context.getString(R.string.backup_details, date, backupFile.messages)
                        newState { copy(selectedBackupDetails = details) }
                    } catch (e: Exception) {
                        newState { copy(showSelectedBackupError = true) }
                    }
                }
    }

}