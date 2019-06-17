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
package com.moez.QKSMS.manager

import android.content.Context
import com.moez.QKSMS.model.SyncLog
import com.moez.QKSMS.util.Preferences
import io.realm.Realm
import javax.inject.Inject

class ChangelogManagerImpl @Inject constructor(
    private val context: Context,
    private val prefs: Preferences
) : ChangelogManager {

    override fun didUpdate(): Boolean {
        val oldVersion = prefs.version.get()
        val newVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        val lastSync = Realm.getDefaultInstance().use { realm -> realm.where(SyncLog::class.java)?.max("date") ?: 0 }

        if (oldVersion != newVersion) {
            prefs.version.set(newVersion)
        }

        return when {
            oldVersion == 0 && lastSync != 0 -> true // Just updated to 3.6.5. TODO: Remove this after 3.6.5
            oldVersion == 0 -> false // First install
            oldVersion != newVersion -> true // Update
            else -> false
        }
    }

}
