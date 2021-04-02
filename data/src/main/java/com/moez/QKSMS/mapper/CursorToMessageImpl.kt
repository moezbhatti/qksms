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

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony.*
import com.google.android.mms.pdu_alt.EncodedStringValue
import com.google.android.mms.pdu_alt.PduHeaders
import com.google.android.mms.pdu_alt.PduPersister
import com.moez.QKSMS.extensions.map
import com.moez.QKSMS.manager.KeyManager
import com.moez.QKSMS.manager.PermissionManager
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.util.Preferences
import com.moez.QKSMS.util.SqliteWrapper
import com.moez.QKSMS.util.tryOrNull
import javax.inject.Inject

class CursorToMessageImpl @Inject constructor(
    private val context: Context,
    private val cursorToPart: CursorToPart,
    private val keys: KeyManager,
    private val permissionManager: PermissionManager,
    private val preferences: Preferences
) : CursorToMessage {

    private val uri = Uri.parse("content://mms-sms/complete-conversations")
    private val projection = arrayOf(
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
            Mms.STATUS
    )

    override fun map(from: Pair<Cursor, CursorToMessage.MessageColumns>): Message {
        val cursor = from.first
        val columnsMap = from.second

        return Message().apply {
            type = when {
                cursor.getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN) != -1 -> cursor.getString(columnsMap.msgType)
                cursor.getColumnIndex(Mms.SUBJECT) != -1 -> "mms"
                cursor.getColumnIndex(Sms.ADDRESS) != -1 -> "sms"
                else -> "unknown"
            }

            id = keys.newId()
            threadId = cursor.getLong(columnsMap.threadId)
            contentId = cursor.getLong(columnsMap.msgId)
            date = cursor.getLong(columnsMap.date)
            dateSent = cursor.getLong(columnsMap.dateSent)
            read = cursor.getInt(columnsMap.read) != 0
            locked = cursor.getInt(columnsMap.locked) != 0
            subId = if (columnsMap.subId != -1) cursor.getInt(columnsMap.subId) else -1

            when (type) {
                "sms" -> {
                    address = cursor.getString(columnsMap.smsAddress) ?: ""
                    boxId = cursor.getInt(columnsMap.smsType)
                    seen = cursor.getInt(columnsMap.smsSeen) != 0

                    body = columnsMap.smsBody
                            .takeIf { column -> column != -1 } // The column may not be set
                            ?.let { column -> cursor.getString(column) } ?: "" // cursor.getString() may return null

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
                    val subjectCharset = cursor.getInt(columnsMap.mmsSubjectCharset)
                    subject = cursor.getString(columnsMap.mmsSubject)
                            ?.takeIf { it.isNotBlank() }
                            ?.let(PduPersister::getBytes)
                            ?.let { EncodedStringValue(subjectCharset, it).string } ?: ""
                    textContentType = ""
                    attachmentType = Message.AttachmentType.NOT_LOADED
                }
            }
        }
    }

    override fun getMessagesCursor(): Cursor? {

        // Even if the device is running API 22, we can't assume we have access to the subId
        // column. In this case, we need to check if we do, before trying to sync messages
        if (!preferences.canUseSubId.isSet) {
            val canUseSubId = tryOrNull {
                SqliteWrapper.query(context, uri, arrayOf(Mms.SUBSCRIPTION_ID), logError = false)?.use { true }
            }

            preferences.canUseSubId.set(canUseSubId ?: false)
        }

        val projection = when (preferences.canUseSubId.get()) {
            true -> this.projection + Mms.SUBSCRIPTION_ID
            false -> this.projection
        }

        return when (permissionManager.hasReadSms()) {
            true -> SqliteWrapper.query(context, uri, projection, sortOrder = "normalized_date desc")
            false -> null
        }
    }

    override fun getMessageCursor(id: Long): Cursor? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
                return cursor.getString(0) ?: ""
            }
        }

        return ""
    }

}
