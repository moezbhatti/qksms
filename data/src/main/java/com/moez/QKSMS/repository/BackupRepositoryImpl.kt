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
import com.moez.QKSMS.model.BackupFile
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.util.QkFileObserver
import com.squareup.moshi.Moshi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.Realm
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BackupRepositoryImpl @Inject constructor(
        private val moshi: Moshi
) : BackupRepository {

    companion object {
        private val BACKUP_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/QKSMS/Backups"
    }

    data class Backup(
            val messages: List<BackupMessage>
    )

    data class BackupMessage(
            val protocol: Int,
            val address: String,
            val date: Long,
            val type: String,
            val body: String,
            val serviceCenter: String?,
            val read: Boolean,
            val status: Int,
            val locked: Boolean,
            val dateSent: Long)

    // Subjects to emit our progress events to
    private val backupProgress: Subject<BackupRepository.Progress> = BehaviorSubject.createDefault(BackupRepository.Progress.Idle())
    private val restoreProgress: Subject<BackupRepository.Progress> = BehaviorSubject.createDefault(BackupRepository.Progress.Idle())

    override fun performBackup() {
        // If a backup or restore is already running, don't do anything
        if (isBackupOrRestoreRunning()) return

        // Map all the messages into our object we'll use for the Json mapping
        val backupMessages = Realm.getDefaultInstance().use { realm ->
            // Get the messages from realm
            val messages = realm.where(Message::class.java).findAll().createSnapshot()
            val messageCount = messages.size

            // Map the messages to the new format
            val startTime = System.currentTimeMillis()
            messages.mapIndexed { index, message ->
                val progress = index.toDouble() / messageCount * 100
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = when {
                    index > 100 -> TimeUnit.MILLISECONDS.toSeconds((elapsed.toDouble() / index * (messageCount - index)).toLong())
                    else -> 0
                }

                // Update the progress
                backupProgress.onNext(BackupRepository.Progress.Running(progress.toInt(), "$remaining seconds remaining"))
                messageToBackupMessage(message)
            }
        }

        // Update the status, and set the progress to be indeterminate since we can no longer calculate progress
        backupProgress.onNext(BackupRepository.Progress.Running(0, "Saving..."))

        // Convert the data to json
        val adapter = moshi.adapter(Backup::class.java).indent("\t")
        val json = adapter.toJson(Backup(backupMessages)).toByteArray()

        try {
            // Create the directory and file
            val dir = File(BACKUP_DIRECTORY).apply { mkdirs() }
            val file = File(dir, "backup-${SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(System.currentTimeMillis())}.json")

            // Write the log to the file
            FileOutputStream(file, true).use { fileOutputStream -> fileOutputStream.write(json) }
        } catch (e: Exception) {
        }

        backupProgress.onNext(BackupRepository.Progress.Idle())
    }

    private fun messageToBackupMessage(message: Message): BackupMessage = BackupMessage(
            protocol = 0,
            address = message.address,
            date = message.date,
            type = message.type,
            body = message.body,
            serviceCenter = null,
            read = message.read,
            status = message.deliveryStatus,
            locked = message.locked,
            dateSent = message.dateSent)

    override fun getBackupProgress(): Observable<BackupRepository.Progress> = backupProgress

    override fun getBackups(): Observable<List<BackupFile>> = QkFileObserver(BACKUP_DIRECTORY).observable
            .map { File(BACKUP_DIRECTORY).listFiles() ?: arrayOf() }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .map { files ->
                files.map { file ->
                    val date = file.lastModified()
                    val messages = 0
                    val size = file.length()
                    BackupFile(date, messages, size)
                }
            }
            .map { files -> files.sortedByDescending { file -> file.date } }

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