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

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.provider.Telephony.*
import com.google.android.mms.pdu_alt.PduHeaders
import common.util.Keys
import common.util.extensions.map
import data.model.Message
import data.model.MmsPart
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class CursorToMessage @Inject constructor(
        private val context: Context,
        private val keys: Keys,
        private val cursorToPart: CursorToPart
) : Mapper<Pair<Cursor, CursorToMessage.MessageColumns>, Message> {

    override fun map(from: Pair<Cursor, MessageColumns>): Message {
        val cursor = from.first
        val columnsMap = from.second

        return Message().apply {
            type = when {
                cursor.getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN) != -1 -> cursor.getString(columnsMap.msgType)
                cursor.getColumnIndex(Sms.ADDRESS) != -1 -> "sms"
                cursor.getColumnIndex(Mms.SUBJECT) != -1 -> "mms"
                else -> "unknown"
            }

            id = keys.newId()
            contentId = cursor.getLong(columnsMap.msgId)
            threadId = cursor.getLong(columnsMap.threadId)
            date = cursor.getLong(columnsMap.date)
            dateSent = cursor.getLong(columnsMap.dateSent)
            read = cursor.getInt(columnsMap.read) != 0
            locked = cursor.getInt(columnsMap.locked) != 0

            when (type) {
                "sms" -> {
                    address = cursor.getString(columnsMap.smsAddress) ?: ""
                    boxId = cursor.getInt(columnsMap.smsType)
                    seen = cursor.getInt(columnsMap.smsSeen) != 0
                    body = if (columnsMap.smsBody != -1) cursor.getString(columnsMap.smsBody) else null ?: ""
                    errorCode = cursor.getInt(columnsMap.smsErrorCode)
                    deliveryStatus = cursor.getInt(columnsMap.smsStatus)
                }

                "mms" -> {
                    address = getMmsAddress(contentId)
                    boxId = cursor.getInt(columnsMap.mmsMessageBox)
                    date *= 1000L
                    dateSent *= 1000L
                    seen = cursor.getInt(columnsMap.mmsSeen) != 0
                    mmsDeliveryStatusString = cursor.getString(columnsMap.mmsDeliveryReport) ?: ""
                    errorType = if (columnsMap.mmsErrorType != -1) cursor.getInt(columnsMap.mmsErrorType) else 0
                    messageSize = 0
                    readReportString = cursor.getString(columnsMap.mmsReadReport) ?: ""
                    messageType = cursor.getInt(columnsMap.mmsMessageType)
                    mmsStatus = cursor.getInt(columnsMap.mmsStatus)
                    subject = cursor.getString(columnsMap.mmsSubject) ?: ""
                    textContentType = ""
                    attachmentType = Message.AttachmentType.NOT_LOADED

                    parts.addAll(getMmsParts(contentId))
                }
            }
        }
    }


    private fun getMmsAddress(messageId: Long): String {
        val uri = Mms.CONTENT_URI.buildUpon()
                .appendPath(messageId.toString())
                .appendPath("addr").build()

        //TODO: Use Charset to ensure address is decoded correctly
        val projection = arrayOf(Mms.Addr.ADDRESS, Mms.Addr.CHARSET)
        val selection = "${Mms.Addr.TYPE} = ${PduHeaders.FROM}"

        val cursor = context.contentResolver.query(uri, projection, selection, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }

        return ""
    }

    private fun getMmsParts(contentId: Long): List<MmsPart> {
        return context.contentResolver.query(CursorToPart.CONTENT_URI, null,
                "${Telephony.Mms.Part.MSG_ID} = ?", arrayOf(contentId.toString()), null)
                ?.map { cursorToPart.map(it) } ?: listOf()
    }

    companion object {
        val URI = Uri.parse("content://mms-sms/complete-conversations")
        val PROJECTION = arrayOf(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN,
                MmsSms._ID,
                Mms.DATE,
                Mms.DATE_SENT,
                Mms.READ,
                Mms.THREAD_ID,
                Mms.LOCKED,

                Sms.ADDRESS,
                Sms.BODY,
                Sms.SEEN,
                Sms.TYPE,
                Sms.STATUS,
                Sms.ERROR_CODE,

                Mms.SUBJECT,
                Mms.SUBJECT_CHARSET,
                Mms.SEEN,
                Mms.MESSAGE_TYPE,
                Mms.MESSAGE_BOX,
                Mms.DELIVERY_REPORT,
                Mms.READ_REPORT,
                MmsSms.PendingMessages.ERROR_TYPE,
                Mms.STATUS)
    }

    class MessageColumns(private val cursor: Cursor) {

        val msgType by lazy { getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN) }
        val msgId by lazy { getColumnIndex(MmsSms._ID) }
        val date by lazy { getColumnIndex(Mms.DATE) }
        val dateSent by lazy { getColumnIndex(Mms.DATE_SENT) }
        val read by lazy { getColumnIndex(Mms.READ) }
        val threadId by lazy { getColumnIndex(Mms.THREAD_ID) }
        val locked by lazy { getColumnIndex(Mms.LOCKED) }

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