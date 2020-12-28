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
package com.moez.QKSMS.common.util

import android.os.Environment
import android.util.Log
import com.moez.QKSMS.util.Preferences
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Based off Vipin Kumar's FileLoggingTree: https://medium.com/@vicky7230/file-logging-with-timber-4e63a1b86a66
 */
@Singleton
class FileLoggingTree @Inject constructor(private val prefs: Preferences) : Timber.DebugTree() {

    private val fileLock: Boolean = false

    override fun log(priority: Int, tag: String, message: String, t: Throwable?) {
        if (!prefs.logging.get()) return

        Schedulers.io().scheduleDirect {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault()).format(System.currentTimeMillis())
            val priorityString = when (priority) {
                Log.VERBOSE -> "V"
                Log.DEBUG -> "D"
                Log.INFO -> "I"
                Log.WARN -> "W"
                Log.ERROR -> "E"
                else -> "WTF"
            }

            // Format the log to be written to the file
            val log = "$timestamp $priorityString/$tag: $message ${Log.getStackTraceString(t)}\n".toByteArray()

            // Ensure that only one thread is writing to the file at a time
            synchronized(fileLock) {
                try {
                    // Create the directory
                    val dir = File(Environment.getExternalStorageDirectory(), "QKSMS/Logs").apply { mkdirs() }

                    // Create the file
                    val file = File(dir, "${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())}.log")

                    // Write the log to the file
                    FileOutputStream(file, true).use { fileOutputStream -> fileOutputStream.write(log) }
                } catch (e: Exception) {
                    Log.e("FileLoggingTree", "Error while logging into file", e)
                }
            }
        }
    }
}