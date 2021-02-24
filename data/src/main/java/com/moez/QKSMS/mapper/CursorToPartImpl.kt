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
import android.provider.Telephony
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import com.moez.QKSMS.model.MmsPart
import javax.inject.Inject

class CursorToPartImpl @Inject constructor(private val context: Context) : CursorToPart {

    companion object {
        val CONTENT_URI: Uri = Uri.parse("content://mms/part")
    }

    override fun map(from: Cursor) = MmsPart().apply {
        id = from.getLong(from.getColumnIndexOrThrow(Telephony.Mms.Part._ID))
        messageId = from.getLong(from.getColumnIndexOrThrow(Telephony.Mms.Part.MSG_ID))
        type = from.getStringOrNull(from.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_TYPE)) ?: "*/*"
        seq = from.getIntOrNull(from.getColumnIndexOrThrow(Telephony.Mms.Part.SEQ)) ?: -1
        name = from.getStringOrNull(from.getColumnIndexOrThrow(Telephony.Mms.Part.NAME))
                ?: from.getStringOrNull(from.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_LOCATION))
                        ?.split("/")?.last()
        text = from.getStringOrNull(from.getColumnIndexOrThrow(Telephony.Mms.Part.TEXT))
    }

    override fun getPartsCursor(messageId: Long?): Cursor? {
        return when (messageId) {
            null -> context.contentResolver.query(CONTENT_URI, null, null, null, null)
            else -> context.contentResolver.query(CONTENT_URI, null,
                    "${Telephony.Mms.Part.MSG_ID} = ?", arrayOf(messageId.toString()), null)
        }
    }

}
