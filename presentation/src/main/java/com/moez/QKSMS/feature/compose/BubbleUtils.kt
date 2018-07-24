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
package com.moez.QKSMS.feature.compose

import com.moez.QKSMS.R
import com.moez.QKSMS.model.Message
import java.util.concurrent.TimeUnit

object BubbleUtils {

    const val TIMESTAMP_THRESHOLD = 10

    fun canGroup(message: Message, other: Message?): Boolean {
        if (other == null) return false
        val diff = TimeUnit.MILLISECONDS.toMinutes(Math.abs(message.date - other.date))
        return message.compareSender(other) && diff < TIMESTAMP_THRESHOLD
    }

    fun getBubble(canGroupWithPrevious: Boolean, canGroupWithNext: Boolean, isMe: Boolean): Int {
        return when {
            !canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_out_first else R.drawable.message_in_first
            canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_out_middle else R.drawable.message_in_middle
            canGroupWithPrevious && !canGroupWithNext -> if (isMe) R.drawable.message_out_last else R.drawable.message_in_last
            else -> R.drawable.message_only
        }
    }

}