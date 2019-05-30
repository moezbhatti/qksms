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
package com.moez.QKSMS.blocking

import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.core.database.getStringOrNull
import com.callcontrol.datashare.CallControl
import com.moez.QKSMS.extensions.map
import com.moez.QKSMS.util.tryOrNull
import io.reactivex.Single
import javax.inject.Inject

class CallControlBlockingClient @Inject constructor(
    private val context: Context
) : BlockingClient {

    class LookupResult(cursor: Cursor) {
        val displayName: String? = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(CallControl.Lookup.DISPLAY_NAME))
        val blockReason: String? = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(CallControl.Lookup.BLOCK_REASON))
    }

    override fun shouldBlock(address: String): Single<Boolean> {
        val uri = Uri.withAppendedPath(CallControl.LOOKUP_TEXT_URI, address)
        return Single.fromCallable {
            tryOrNull {
                context.contentResolver.query(uri, null, null, null, null) // Query URI
                        ?.use { cursor -> cursor.map(::LookupResult) } // Map to Result object
                        ?.any { result -> result.blockReason != null } // Check if any are blocked
            } == true // If none are blocked or we errored at some point, return false
        }
    }

}
