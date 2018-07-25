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
package com.moez.QKSMS.util

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import timber.log.Timber

/**
 * Utility class to make ContentResolver queries with Kotlin more concise
 */
object SqliteWrapper {

    fun query(
            context: Context,
            uri: Uri,
            projection: Array<String>? = null,
            selection: String? = null,
            selectionArgs: Array<String>? = null,
            sortOrder: String? = null,
            logError: Boolean = true
    ): Cursor? {
        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        } catch (e: SQLiteException) {
            if (logError) {
                Timber.e(e)
            }
            null
        }

    }

}