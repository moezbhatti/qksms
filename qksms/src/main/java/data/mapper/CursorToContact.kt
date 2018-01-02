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
import android.provider.ContactsContract
import data.model.Contact
import data.model.PhoneNumber
import javax.inject.Inject

class CursorToContact @Inject constructor() : Mapper<Cursor, Contact> {

    companion object {
        val URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

        val LOOKUP_KEY = 0
        val NUMBER = 1
        val DISPLAY_NAME = 2
    }

    override fun map(from: Cursor) = Contact().apply {
        lookupKey = from.getString(LOOKUP_KEY)
        name = from.getString(DISPLAY_NAME)
        numbers.add(PhoneNumber().apply { address = from.getString(NUMBER) })
        lastUpdate = System.currentTimeMillis()
    }

}