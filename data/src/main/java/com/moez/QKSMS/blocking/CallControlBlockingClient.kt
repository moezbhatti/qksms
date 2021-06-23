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
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.core.database.getStringOrNull
import com.callcontrol.datashare.CallControl
import com.moez.QKSMS.common.util.extensions.isInstalled
import com.moez.QKSMS.extensions.map
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class CallControlBlockingClient @Inject constructor(
    private val context: Context
) : BlockingClient {

    private val projection: Array<String> = arrayOf(
            //CallControl.Lookup.DISPLAY_NAME, // This has a performance impact on the lookup, and we don't need it
            CallControl.Lookup.BLOCK_REASON
    )

    class LookupResult(cursor: Cursor) {
        val blockReason: String? = cursor.getStringOrNull(0)
    }

    override fun isAvailable(): Boolean = context.isInstalled("com.flexaspect.android.everycallcontrol")

    override fun getClientCapability() = BlockingClient.Capability.BLOCK_WITH_PERMISSION

    override fun shouldBlock(address: String): Single<BlockingClient.Action> = isBlacklisted(address)

    override fun isBlacklisted(address: String): Single<BlockingClient.Action> = Single.fromCallable {
        val uri = Uri.withAppendedPath(CallControl.LOOKUP_TEXT_URI, address)
        return@fromCallable try {
            val blockReason = context.contentResolver.query(uri, projection, null, null, null) // Query URI
                    ?.use { cursor -> cursor.map(::LookupResult) } // Map to Result object
                    ?.find { result -> result.blockReason != null } // Check if any are blocked
                    ?.blockReason // If none are blocked or we errored at some point, return false

            when (blockReason) {
                null -> BlockingClient.Action.Unblock
                else -> BlockingClient.Action.Block(blockReason)
            }
        } catch (e: Exception) {
            Timber.w(e)
            BlockingClient.Action.DoNothing
        }
    }

    override fun block(addresses: List<String>): Completable = Completable.fromCallable {
        val reports = addresses.map { CallControl.Report(it) }
        val reportsArrayList = arrayListOf<CallControl.Report>().apply { addAll(reports) }
        CallControl.addRule(context, reportsArrayList, Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    override fun unblock(addresses: List<String>): Completable = Completable.fromCallable {
        val reports = addresses.map { CallControl.Report(it, null, false) }
        val reportsArrayList = arrayListOf<CallControl.Report>().apply { addAll(reports) }
        CallControl.addRule(context, reportsArrayList, Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    override fun openSettings() {
        CallControl.openBlockedList(context, Intent.FLAG_ACTIVITY_NEW_TASK)
    }

}
