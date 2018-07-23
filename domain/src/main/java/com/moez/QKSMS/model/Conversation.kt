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
package com.moez.QKSMS.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class Conversation(
        @PrimaryKey var id: Long = 0,
        @Index var archived: Boolean = false,
        @Index var blocked: Boolean = false,
        @Index var pinned: Boolean = false,
        var recipients: RealmList<Recipient> = RealmList(),
        var count: Int = 0,
        var date: Long = 0,
        var snippet: String = "",
        var read: Boolean = true,
        var me: Boolean = false,
        var draft: String = "",

        // For group chats, the user is allowed to set a custom title for the conversation
        var name: String = ""
) : RealmObject() {

    fun getTitle(): String {
        return name.takeIf { it.isNotBlank() } ?: recipients.joinToString { recipient -> recipient.getDisplayName() }
    }

}
