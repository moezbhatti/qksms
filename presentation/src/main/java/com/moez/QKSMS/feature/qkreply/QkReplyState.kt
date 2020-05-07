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
package com.moez.QKSMS.feature.qkreply

import com.moez.QKSMS.compat.SubscriptionInfoCompat
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Message
import io.realm.RealmResults

data class QkReplyState(
    val hasError: Boolean = false,
    val threadId: Long = 0,
    val title: String = "",
    val expanded: Boolean = false,
    val data: Pair<Conversation, RealmResults<Message>>? = null,
    val remaining: String = "",
    val subscription: SubscriptionInfoCompat? = null,
    val canSend: Boolean = false
)