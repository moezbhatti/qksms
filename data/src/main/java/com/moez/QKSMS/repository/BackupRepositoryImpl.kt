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
package com.moez.QKSMS.repository

import android.animation.ObjectAnimator
import android.os.Environment
import androidx.core.animation.addListener
import com.moez.QKSMS.model.Backup
import com.moez.QKSMS.util.QkFileObserver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor() : BackupRepository {

    companion object {
        private val BACKUP_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/QKSMS/Backups"
    }

    data class BackupMessage(
            val protocol: Int,
            val address: String,
            val date: Long,
            val type: Int,
            val body: String,
            val service_center: String?,
            val read: Boolean,
            val status: Int,
            val locked: Boolean,
            val date_sent: Long)

    private val backupProgress: Subject<BackupRepository.Progress> = BehaviorSubject.createDefault(BackupRepository.Progress.Idle())
    private val restoreProgress: Subject<BackupRepository.Progress> = BehaviorSubject.createDefault(BackupRepository.Progress.Idle())

    override fun performBackup() {
        // If a backup or restore is already running, don't do anything
        if (isBackupOrRestoreRunning()) return

        backupProgress.onNext(BackupRepository.Progress.Running(0))

        Thread.sleep(1000)

        val animator = ObjectAnimator.ofInt(0, 100)
        animator.duration = 3000
        animator.addUpdateListener {
            val remaining = TimeUnit.MILLISECONDS.toSeconds(it.duration - it.currentPlayTime) + 1
            backupProgress.onNext(BackupRepository.Progress.Running(it.animatedValue as Int, "$remaining seconds remaining"))
        }
        animator.addListener(onEnd = {
            backupProgress.onNext(BackupRepository.Progress.Idle())
        })
        AndroidSchedulers.mainThread().scheduleDirect { animator.start() }
    }

    override fun getBackupProgress(): Observable<BackupRepository.Progress> = backupProgress

    override fun getBackups(): Observable<List<Backup>> = QkFileObserver(BACKUP_DIRECTORY)
            .observable
            .map { path -> File(path).listFiles() }
            .map { files ->
                files.map { file ->
                    val date = file.lastModified()
                    val size = file.length()
                    Backup(date, listOf(), size)
                }
            }

    override fun performRestore() {
        // If a backup or restore is already running, don't do anything
        if (isBackupOrRestoreRunning()) return

        restoreProgress.onNext(BackupRepository.Progress.Running(0))

        Thread.sleep(1000)

        val animator = ObjectAnimator.ofInt(0, 100)
        animator.duration = 3000
        animator.addUpdateListener {
            val remaining = TimeUnit.MILLISECONDS.toSeconds(it.duration - it.currentPlayTime) + 1
            restoreProgress.onNext(BackupRepository.Progress.Running(it.animatedValue as Int, "$remaining seconds remaining"))
        }
        animator.addListener(onEnd = {
            restoreProgress.onNext(BackupRepository.Progress.Idle())
        })
        AndroidSchedulers.mainThread().scheduleDirect { animator.start() }
    }

    override fun getRestoreProgress(): Observable<BackupRepository.Progress> = restoreProgress

    private fun isBackupOrRestoreRunning(): Boolean {
        return backupProgress.blockingFirst() is BackupRepository.Progress.Running
                || restoreProgress.blockingFirst() is BackupRepository.Progress.Running
    }

}