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

import android.os.Environment
import com.moez.QKSMS.model.Backup
import com.moez.QKSMS.util.QkFileObserver
import io.reactivex.Observable
import java.io.File
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor() : BackupRepository {

    companion object {
        private val BACKUP_LOCATION = Environment.getExternalStorageDirectory().toString() + "/QKSMS/Backups"
    }

    override fun getBackups(): Observable<List<Backup>> = QkFileObserver(BACKUP_LOCATION)
            .observable
            .map { path -> File(path).listFiles() }
            .map { files ->
                files.map { file ->
                    val date = file.lastModified()
                    val size = file.length()
                    Backup(date, listOf(), size)
                }
            }

}