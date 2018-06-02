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
package feature.compose

import android.telephony.SubscriptionInfo
import io.realm.RealmResults
import model.Attachment
import model.Contact
import model.Conversation
import model.Message

data class ComposeState(
        val hasError: Boolean = false,
        val editingMode: Boolean = false,
        val contacts: List<Contact> = ArrayList(),
        val contactsVisible: Boolean = false,
        val selectedConversation: Long = 0,
        val selectedContacts: List<Contact> = ArrayList(),
        val conversationtitle: String = "",
        val query: String = "",
        val searchSelectionId: Long = -1,
        val searchSelectionPosition: Int = 0,
        val searchResults: Int = 0,
        val messages: Pair<Conversation, RealmResults<Message>>? = null,
        val selectedMessages: Int = 0,
        val attachments: List<Attachment> = ArrayList(),
        val attaching: Boolean = false,
        val remaining: String = "",
        val subscription: SubscriptionInfo? = null,
        val canSend: Boolean = false
)