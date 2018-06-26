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
package com.moez.QKSMS.mapper

import android.database.Cursor
import android.provider.Telephony.*
import com.moez.QKSMS.model.Message
import timber.log.Timber
import java.util.*

interface CursorToMessage : Mapper<Pair<Cursor, CursorToMessage.MessageColumns>, Message> {

    fun getMessagesCursor(): Cursor?

    fun getMessageCursor(id: Long): Cursor?

    class MessageColumns(private val cursor: Cursor) {

        val msgType by lazy { getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN) }
        val msgId by lazy { getColumnIndex(MmsSms._ID) }
        val date by lazy { getColumnIndex(Mms.DATE) }
        val dateSent by lazy { getColumnIndex(Mms.DATE_SENT) }
        val read by lazy { getColumnIndex(Mms.READ) }
        val threadId by lazy { getColumnIndex(Mms.THREAD_ID) }
        val locked by lazy { getColumnIndex(Mms.LOCKED) }
        val subId by lazy { getColumnIndex(Mms.SUBSCRIPTION_ID) }

        val smsAddress by lazy { getColumnIndex(Sms.ADDRESS) }
        val smsBody by lazy { getColumnIndex(Sms.BODY) }
        val smsSeen by lazy { getColumnIndex(Sms.SEEN) }
        val smsType by lazy { getColumnIndex(Sms.TYPE) }
        val smsStatus by lazy { getColumnIndex(Sms.STATUS) }
        val smsErrorCode by lazy { getColumnIndex(Sms.ERROR_CODE) }

        val mmsSubject by lazy { getColumnIndex(Mms.SUBJECT) }
        val mmsSubjectCharset by lazy { getColumnIndex(Mms.SUBJECT_CHARSET) }
        val mmsSeen by lazy { getColumnIndex(Mms.SEEN) }
        val mmsMessageType by lazy { getColumnIndex(Mms.MESSAGE_TYPE) }
        val mmsMessageBox by lazy { getColumnIndex(Mms.MESSAGE_BOX) }
        val mmsDeliveryReport by lazy { getColumnIndex(Mms.DELIVERY_REPORT) }
        val mmsReadReport by lazy { getColumnIndex(Mms.READ_REPORT) }
        val mmsErrorType by lazy { getColumnIndex(MmsSms.PendingMessages.ERROR_TYPE) }
        val mmsStatus by lazy { getColumnIndex(Mms.STATUS) }

        private fun getColumnIndex(columnsName: String) = try {
            cursor.getColumnIndexOrThrow(columnsName)
        } catch (e: Exception) {
            Timber.e("Couldn't find column \'$columnsName\' in ${Arrays.toString(cursor.columnNames)}")
            -1
        }
    }
}
