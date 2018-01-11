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
package data.mapper

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import data.model.MmsPart
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class CursorToPart @Inject constructor(private val context: Context) : Mapper<Cursor, MmsPart> {

    companion object {
        val CONTENT_URI: Uri = Uri.parse("content://mms/part")
    }

    override fun map(from: Cursor) = MmsPart().apply {
        id = from.getLong(from.getColumnIndexOrThrow(Telephony.Mms.Part._ID))
        messageId = from.getLong(from.getColumnIndexOrThrow(Telephony.Mms.Part.MSG_ID))
        type = from.getString(from.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_TYPE))

        val data = from.getString(from.getColumnIndexOrThrow(Telephony.Mms.Part._DATA))

        when {
            isImage() -> image = ContentUris.withAppendedId(CONTENT_URI, id).toString()

            isText() -> {
                text = if (data == null) {
                    from.getString(from.getColumnIndexOrThrow("text"))
                } else {
                    val uri = ContentUris.withAppendedId(CONTENT_URI, id)
                    val sb = StringBuilder()
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val isr = InputStreamReader(inputStream, "UTF-8")
                        val reader = BufferedReader(isr)
                        var temp = reader.readLine()
                        while (temp != null) {
                            sb.append(temp)
                            temp = reader.readLine()
                        }
                    }
                    sb.toString()
                }
            }

            else -> Timber.v("Unhandled type: $type")
        }
    }

}