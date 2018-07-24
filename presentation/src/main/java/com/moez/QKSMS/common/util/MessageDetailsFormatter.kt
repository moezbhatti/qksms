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
package com.moez.QKSMS.common.util

import android.content.Context
import com.google.android.mms.pdu_alt.EncodedStringValue
import com.google.android.mms.pdu_alt.MultimediaMessagePdu
import com.google.android.mms.pdu_alt.PduPersister
import com.moez.QKSMS.R
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.util.tryOrNull
import javax.inject.Inject

class MessageDetailsFormatter @Inject constructor(
        private val context: Context,
        private val dateFormatter: DateFormatter
) {

    fun format(message: Message): String {
        val builder = StringBuilder()

        message.type
                .takeIf { it.isNotBlank() }
                ?.toUpperCase()
                ?.let { context.getString(R.string.compose_details_type, it) }
                ?.let(builder::appendln)

        if (message.isSms()) {
            message.address
                    .takeIf { it.isNotBlank() && !message.isMe() }
                    ?.let { context.getString(R.string.compose_details_from, it) }
                    ?.let(builder::appendln)

            message.address
                    .takeIf { it.isNotBlank() && message.isMe() }
                    ?.let { context.getString(R.string.compose_details_to, it) }
                    ?.let(builder::appendln)
        } else {
            val pdu = tryOrNull {
                PduPersister.getPduPersister(context)
                        .load(message.getUri())
                        as MultimediaMessagePdu
            }

            pdu?.from?.string
                    ?.takeIf { it.isNotBlank() }
                    ?.let { context.getString(R.string.compose_details_from, it) }
                    ?.let(builder::appendln)

            pdu?.to
                    ?.let(EncodedStringValue::concat)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { context.getString(R.string.compose_details_to, it) }
                    ?.let(builder::appendln)
        }

        message.date
                .takeIf { it > 0 && message.isMe() }
                ?.let(dateFormatter::getDetailedTimestamp)
                ?.let { context.getString(R.string.compose_details_sent, it) }
                ?.let(builder::appendln)

        message.dateSent
                .takeIf { it > 0 && !message.isMe() }
                ?.let(dateFormatter::getDetailedTimestamp)
                ?.let { context.getString(R.string.compose_details_sent, it) }
                ?.let(builder::appendln)

        message.date
                .takeIf { it > 0 && !message.isMe() }
                ?.let(dateFormatter::getDetailedTimestamp)
                ?.let { context.getString(R.string.compose_details_received, it) }
                ?.let(builder::appendln)

        message.dateSent
                .takeIf { it > 0 && message.isMe() }
                ?.let(dateFormatter::getDetailedTimestamp)
                ?.let { context.getString(R.string.compose_details_delivered, it) }
                ?.let(builder::appendln)

        message.errorCode
                .takeIf { it != 0 && message.isSms() }
                ?.let { context.getString(R.string.compose_details_error_code, it) }
                ?.let(builder::appendln)

        return builder.toString().trim()
    }

}