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
package com.moez.QKSMS.mapper

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony.Threads
import com.moez.QKSMS.manager.PermissionManager
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Recipient
import javax.inject.Inject

class CursorToConversationImpl @Inject constructor(
        private val context: Context,
        private val permissionManager: PermissionManager
) : CursorToConversation {

    companion object {
        val URI: Uri = Uri.parse("content://mms-sms/conversations?simple=true")
        val PROJECTION = arrayOf(
                Threads._ID,
                Threads.DATE,
                Threads.RECIPIENT_IDS,
                Threads.MESSAGE_COUNT,
                Threads.READ,
                Threads.SNIPPET)

        const val ID = 0
        const val DATE = 1
        const val RECIPIENT_IDS = 2
        const val MESSAGE_COUNT = 3
        const val READ = 4
        const val SNIPPET = 5
    }

    override fun map(from: Cursor): Conversation {
        return Conversation().apply {
            id = from.getLong(ID)
            date = from.getLong(DATE)
            recipients.addAll(from.getString(RECIPIENT_IDS)
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .map { recipientId -> recipientId.toLong() }
                    .map { recipientId -> Recipient().apply { id = recipientId } })
            count = from.getInt(MESSAGE_COUNT)
            read = from.getInt(READ) == 1
            snippet = from.getString(SNIPPET) ?: ""
        }
    }

    override fun getConversationsCursor(lastSync: Long): Cursor? {
        return when (permissionManager.hasReadSms()) {
            true -> context.contentResolver.query(URI, PROJECTION,
                    "date > $lastSync", null,
                    "date desc")

            false -> null
        }
    }

}