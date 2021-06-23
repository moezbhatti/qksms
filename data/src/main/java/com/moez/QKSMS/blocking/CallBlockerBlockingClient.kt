/*
 * Copyright (C) 2020 Moez Bhatti <moez.bhatti@gmail.com>
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
import com.moez.QKSMS.common.util.extensions.isInstalled
import com.moez.QKSMS.extensions.map
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class CallBlockerBlockingClient @Inject constructor(
    private val context: Context
) : BlockingClient {

    class LookupResult(cursor: Cursor) {
        val blockReason: String? = cursor.getStringOrNull(0)
    }

    override fun isAvailable(): Boolean = context.isInstalled("com.cuiet.blockCalls")

    override fun getClientCapability() = BlockingClient.Capability.BLOCK_WITH_PERMISSION

    override fun shouldBlock(address: String): Single<BlockingClient.Action> = lookup(address, "incomingNumber")

    override fun isBlacklisted(address: String): Single<BlockingClient.Action> = lookup(address, "blacklistLookup")

    private fun lookup(address: String, reason: String): Single<BlockingClient.Action> = Single.fromCallable {
        val uri = Uri.parse("content://com.cuiet.blockCalls.ContProvBlockCalls/lookup/is.blocked.lookup")
        return@fromCallable try {
            val blockReason = context.contentResolver.query(uri, arrayOf("result"), reason, arrayOf(address), null)
                    ?.use { cursor -> cursor.map(::LookupResult) }
                    ?.find { result -> result.blockReason != null }
                    ?.blockReason

            when (blockReason) {
                "true" -> BlockingClient.Action.Block()
                else -> BlockingClient.Action.Unblock
            }
        } catch (e: Exception) {
            Timber.w(e)
            BlockingClient.Action.DoNothing
        }
    }

    override fun block(addresses: List<String>): Completable = Completable.fromCallable {
        val arrayList = ArrayList<String>()
        arrayList.addAll(addresses)
        val intent = Intent("com.cuiet.blockCalls.ADD_NUMBERS")
        intent.putStringArrayListExtra("addresses", arrayList)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun unblock(addresses: List<String>): Completable = Completable.fromCallable {
        val arrayList = ArrayList<String>()
        arrayList.addAll(addresses)
        val intent = Intent("com.cuiet.blockCalls.REMOVE_NUMBERS")
        intent.putStringArrayListExtra("addresses", arrayList)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun openSettings() {
        val intent = Intent("com.cuiet.blockCalls.OPEN_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
