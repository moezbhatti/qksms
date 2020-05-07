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
import javax.inject.Inject

class CursorToContactGroupMemberImpl @Inject constructor(
    private val context: Context
) : CursorToContactGroupMember {

    companion object {
        private val URI = ContactsContract.Data.CONTENT_URI
        private val PROJECTION = arrayOf(
                ContactsContract.Data.LOOKUP_KEY,
                ContactsContract.Data.DATA1)

        private const val SELECTION = "${ContactsContract.Data.MIMETYPE}=?"
        private val SELECTION_ARGS = arrayOf(
                ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)

        private const val LOOKUP_KEY = 0
        private const val GROUP_ID = 1
    }

    override fun map(from: Cursor): CursorToContactGroupMember.GroupMember {
        return CursorToContactGroupMember.GroupMember(from.getString(LOOKUP_KEY), from.getLong(GROUP_ID))
    }

    override fun getGroupMembersCursor(): Cursor? {
        return context.contentResolver.query(URI, PROJECTION, SELECTION, SELECTION_ARGS, null)
    }

}
