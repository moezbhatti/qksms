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
import android.provider.Telephony.*
import data.model.Message
import data.model.Message.DeliveryStatus
import timber.log.Timber
import javax.inject.Inject

class CursorToMessage @Inject constructor(val context: Context) : Mapper<Pair<Cursor, CursorToMessage.MessageColumns>, Message> {

    override fun map(from: Pair<Cursor, MessageColumns>): Message {
        val cursor = from.first
        val columnsMap = from.second

        return Message().apply {
            type = when (cursor.getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN)) {
                -1 -> "sms"
                else -> cursor.getString(columnsMap.msgType)
            }

            id = cursor.getLong(columnsMap.msgId)

            when (type) {
                "sms" -> {
                    threadId = cursor.getLong(columnsMap.smsThreadId)
                    address = cursor.getString(columnsMap.smsAddress) ?: ""
                    boxId = cursor.getInt(columnsMap.smsType)
                    date = cursor.getLong(columnsMap.smsDate)
                    dateSent = cursor.getLong(columnsMap.smsDateSent)
                    read = cursor.getInt(columnsMap.smsRead) != 0
                    seen = cursor.getInt(columnsMap.smsSeen) != 0
                    locked = cursor.getInt(columnsMap.smsLocked) != 0

                    val status = cursor.getLong(columnsMap.smsStatus)
                    deliveryStatus = when {
                        status == Sms.STATUS_NONE.toLong() -> DeliveryStatus.NONE
                        status >= Sms.STATUS_FAILED -> DeliveryStatus.FAILED
                        status >= Sms.STATUS_PENDING -> DeliveryStatus.PENDING
                        else -> DeliveryStatus.RECEIVED
                    }

                    // SMS Specific
                    body = cursor.getString(columnsMap.smsBody) ?: ""
                    errorCode = cursor.getInt(columnsMap.smsErrorCode)
                }

                "mms" -> {
                    threadId = cursor.getLong(columnsMap.mmsThreadId)
                    address = getMmsAddress(id)
                    boxId = cursor.getInt(columnsMap.mmsMessageBox)
                    date = cursor.getLong(columnsMap.mmsDate) * 1000L
                    dateSent = cursor.getLong(columnsMap.mmsDateSent)
                    read = cursor.getInt(columnsMap.mmsRead) != 0
                    seen = cursor.getInt(columnsMap.mmsSeen) != 0
                    locked = cursor.getInt(columnsMap.mmsLocked) != 0

                    // MMS Specific
                    attachmentType = when (cursor.getInt(columnsMap.mmsTextOnly)) {
                        0 -> Message.AttachmentType.NOT_LOADED
                        else -> Message.AttachmentType.TEXT
                    }
                    mmsDeliveryStatusString = cursor.getString(columnsMap.mmsDeliveryReport) ?: ""
                    errorType = cursor.getInt(columnsMap.mmsErrorType)
                    messageSize = 0
                    readReportString = cursor.getString(columnsMap.mmsReadReport) ?: ""
                    messageType = cursor.getInt(columnsMap.mmsMessageType)
                    mmsStatus = cursor.getInt(columnsMap.mmsStatus)
                    subject = cursor.getString(columnsMap.mmsSubject) ?: ""
                    textContentType = ""
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
        val selection = "${Mms.Addr.TYPE} = 137"

        val cursor = context.contentResolver.query(uri, projection, selection, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }

        return ""
    }

    companion object {
        val URI = Uri.parse("content://mms-sms/complete-conversations")
        val PROJECTION = arrayOf(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN,
                MmsSms._ID,

                Sms.THREAD_ID,
                Sms.ADDRESS,
                Sms.BODY,
                Sms.DATE,
                Sms.DATE_SENT,
                Sms.SEEN,
                Sms.READ,
                Sms.TYPE,
                Sms.STATUS,
                Sms.LOCKED,
                Sms.ERROR_CODE,

                Mms.THREAD_ID,
                Mms.Addr.ADDRESS,
                Mms.SUBJECT,
                Mms.SUBJECT_CHARSET,
                Mms.DATE,
                Mms.DATE_SENT,
                Mms.SEEN,
                Mms.READ,
                Mms.MESSAGE_TYPE,
                Mms.MESSAGE_BOX,
                Mms.DELIVERY_REPORT,
                Mms.READ_REPORT,
                MmsSms.PendingMessages.ERROR_TYPE,
                Mms.LOCKED,
                Mms.STATUS,
                Mms.TEXT_ONLY)
    }

    class MessageColumns(private val cursor: Cursor) {

        val msgType by lazy { getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN) }
        val msgId by lazy { getColumnIndex(MmsSms._ID) }

        val smsThreadId by lazy { getColumnIndex(Sms.THREAD_ID) }
        val smsAddress by lazy { getColumnIndex(Sms.ADDRESS) }
        val smsBody by lazy { getColumnIndex(Sms.BODY) }
        val smsDate by lazy { getColumnIndex(Sms.DATE) }
        val smsDateSent by lazy { getColumnIndex(Sms.DATE_SENT) }
        val smsSeen by lazy { getColumnIndex(Sms.SEEN) }
        val smsRead by lazy { getColumnIndex(Sms.READ) }
        val smsType by lazy { getColumnIndex(Sms.TYPE) }
        val smsStatus by lazy { getColumnIndex(Sms.STATUS) }
        val smsLocked by lazy { getColumnIndex(Sms.LOCKED) }
        val smsErrorCode by lazy { getColumnIndex(Sms.ERROR_CODE) }

        val mmsThreadId by lazy { getColumnIndex(Mms.THREAD_ID) }
        val mmsAddress by lazy { getColumnIndex(Mms.Addr.ADDRESS) }
        val mmsSubject by lazy { getColumnIndex(Mms.SUBJECT) }
        val mmsSubjectCharset by lazy { getColumnIndex(Mms.SUBJECT_CHARSET) }
        val mmsDate by lazy { getColumnIndex(Mms.DATE) }
        val mmsDateSent by lazy { getColumnIndex(Mms.DATE_SENT) }
        val mmsSeen by lazy { getColumnIndex(Mms.SEEN) }
        val mmsRead by lazy { getColumnIndex(Mms.READ) }
        val mmsMessageType by lazy { getColumnIndex(Mms.MESSAGE_TYPE) }
        val mmsMessageBox by lazy { getColumnIndex(Mms.MESSAGE_BOX) }
        val mmsDeliveryReport by lazy { getColumnIndex(Mms.DELIVERY_REPORT) }
        val mmsReadReport by lazy { getColumnIndex(Mms.READ_REPORT) }
        val mmsErrorType by lazy { getColumnIndex(MmsSms.PendingMessages.ERROR_TYPE) }
        val mmsLocked by lazy { getColumnIndex(Mms.LOCKED) }
        val mmsStatus by lazy { getColumnIndex(Mms.STATUS) }
        val mmsTextOnly by lazy { getColumnIndex(Mms.TEXT_ONLY) }

        private fun getColumnIndex(columnsName: String) = try {
            cursor.getColumnIndexOrThrow(columnsName)
        } catch (e: Exception) {
            Timber.w(e)
            -1
        }
    }
}