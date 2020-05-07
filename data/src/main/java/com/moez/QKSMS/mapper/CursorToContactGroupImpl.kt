/*
 * Copyright (C) 2019 Moez Bhatti <moez.bhatti@gmail.com>
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
import android.provider.ContactsContract
import com.moez.QKSMS.model.ContactGroup
import javax.inject.Inject

class CursorToContactGroupImpl @Inject constructor(
    private val context: Context
) : CursorToContactGroup {

    companion object {
        private val URI = ContactsContract.Groups.CONTENT_URI
        private val PROJECTION = arrayOf(
                ContactsContract.Groups._ID,
                ContactsContract.Groups.TITLE)
        private const val SELECTION = "${ContactsContract.Groups.AUTO_ADD}=0 " +
                "AND ${ContactsContract.Groups.DELETED}=0 " +
                "AND ${ContactsContract.Groups.FAVORITES}=0 " +
                "AND ${ContactsContract.Groups.TITLE} IS NOT NULL"

        private const val ID = 0
        private const val TITLE = 1
    }

    override fun map(from: Cursor): ContactGroup {
        return ContactGroup(from.getLong(ID), from.getString(TITLE))
    }

    override fun getContactGroupsCursor(): Cursor? {
        return context.contentResolver.query(URI, PROJECTION, SELECTION, null, null)
    }

}
