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

import android.database.Cursor
import android.net.Uri
import android.telephony.PhoneNumberUtils
import data.model.Recipient
import javax.inject.Inject

class CursorToRecipient @Inject constructor() : Mapper<Cursor, Recipient> {

    companion object {
        val URI = Uri.parse("content://mms-sms/canonical-addresses")

        val COLUMN_ID = 0
        val COLUMN_ADDRESS = 1
    }

    override fun map(from: Cursor) = Recipient().apply {
        id = from.getLong(COLUMN_ID)
        address = PhoneNumberUtils.stripSeparators(from.getString(COLUMN_ADDRESS))
        lastUpdate = System.currentTimeMillis()
    }

}