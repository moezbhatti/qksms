package com.moez.QKSMS.data.mapper

import android.database.Cursor
import android.provider.Telephony
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.model.Message
import io.reactivex.Flowable
import timber.log.Timber

object CursorToMessageFlowable {

    fun map(cursor: Cursor): Flowable<Message> {
        val messageColumns = MessageColumns(cursor)
        return cursor.asFlowable().map { cursor -> cursorToMessage(cursor, messageColumns) }
    }

    private fun cursorToMessage(cursor: Cursor, columnsMap: MessageColumns): Message {
        return Message().apply {
            type = when (cursor.getColumnIndex(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN)) {
                -1 -> "sms"
                else -> cursor.getString(columnsMap.msgType)
            }

            id = cursor.getLong(columnsMap.msgId)
            body = cursor.getString(columnsMap.smsBody) ?: ""

            when (type) {
                "sms" -> {
                    threadId = cursor.getLong(columnsMap.smsThreadId)
                    address = cursor.getString(columnsMap.smsAddress)
                    boxId = cursor.getInt(columnsMap.smsType)
                    date = cursor.getLong(columnsMap.mmsDate)
                    dateSent = cursor.getLong(columnsMap.mmsDateSent)
                    seen = cursor.getInt(columnsMap.mmsSeen) != 0
                    read = cursor.getInt(columnsMap.mmsRead) != 0
                }

                "mms" -> {
                    threadId = cursor.getLong(columnsMap.mmsThreadId)
                    boxId = cursor.getInt(columnsMap.mmsMessageBox)
                    date = cursor.getLong(columnsMap.smsDate)
                    dateSent = cursor.getLong(columnsMap.smsDateSent)
                    seen = cursor.getInt(columnsMap.smsSeen) != 0
                    read = cursor.getInt(columnsMap.smsRead) != 0
                    errorType = cursor.getInt(columnsMap.mmsErrorType)
                }
            }
        }
    }

    val CURSOR_PROJECTION = arrayOf(
            Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN,
            Telephony.MmsSms._ID,

            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.DATE_SENT,
            Telephony.Sms.SEEN,
            Telephony.Sms.READ,
            Telephony.Sms.TYPE,
            Telephony.Sms.STATUS,
            Telephony.Sms.LOCKED,
            Telephony.Sms.ERROR_CODE,

            Telephony.Mms.THREAD_ID,
            Telephony.Mms.SUBJECT,
            Telephony.Mms.SUBJECT_CHARSET,
            Telephony.Mms.DATE,
            Telephony.Mms.DATE_SENT,
            Telephony.Mms.SEEN,
            Telephony.Mms.READ,
            Telephony.Mms.MESSAGE_TYPE,
            Telephony.Mms.MESSAGE_BOX,
            Telephony.Mms.DELIVERY_REPORT,
            Telephony.Mms.READ_REPORT,
            Telephony.MmsSms.PendingMessages.ERROR_TYPE,
            Telephony.Mms.LOCKED,
            Telephony.Mms.STATUS,
            Telephony.Mms.TEXT_ONLY)

    private class MessageColumns(val cursor: Cursor) {

        val msgType by lazy { getColumnIndex(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN) }
        val msgId by lazy { getColumnIndex(Telephony.MmsSms._ID) }

        val smsThreadId by lazy { getColumnIndex(Telephony.Sms.THREAD_ID) }
        val smsAddress by lazy { getColumnIndex(Telephony.Sms.ADDRESS) }
        val smsBody by lazy { getColumnIndex(Telephony.Sms.BODY) }
        val smsDate by lazy { getColumnIndex(Telephony.Sms.DATE) }
        val smsDateSent by lazy { getColumnIndex(Telephony.Sms.DATE_SENT) }
        val smsSeen by lazy { getColumnIndex(Telephony.Sms.SEEN) }
        val smsRead by lazy { getColumnIndex(Telephony.Sms.READ) }
        val smsType by lazy { getColumnIndex(Telephony.Sms.TYPE) }
        val smsStatus by lazy { getColumnIndex(Telephony.Sms.STATUS) }
        val smsLocked by lazy { getColumnIndex(Telephony.Sms.LOCKED) }
        val smsErrorCode by lazy { getColumnIndex(Telephony.Sms.ERROR_CODE) }

        val mmsThreadId by lazy { getColumnIndex(Telephony.Mms.THREAD_ID) }
        val mmsSubject by lazy { getColumnIndex(Telephony.Mms.SUBJECT) }
        val mmsSubjectCharset by lazy { getColumnIndex(Telephony.Mms.SUBJECT_CHARSET) }
        val mmsDate by lazy { getColumnIndex(Telephony.Mms.DATE) }
        val mmsDateSent by lazy { getColumnIndex(Telephony.Mms.DATE_SENT) }
        val mmsSeen by lazy { getColumnIndex(Telephony.Mms.SEEN) }
        val mmsRead by lazy { getColumnIndex(Telephony.Mms.READ) }
        val mmsMessageType by lazy { getColumnIndex(Telephony.Mms.MESSAGE_TYPE) }
        val mmsMessageBox by lazy { getColumnIndex(Telephony.Mms.MESSAGE_BOX) }
        val mmsDeliveryReport by lazy { getColumnIndex(Telephony.Mms.DELIVERY_REPORT) }
        val mmsReadReport by lazy { getColumnIndex(Telephony.Mms.READ_REPORT) }
        val mmsErrorType by lazy { getColumnIndex(Telephony.MmsSms.PendingMessages.ERROR_TYPE) }
        val mmsLocked by lazy { getColumnIndex(Telephony.Mms.LOCKED) }
        val mmsStatus by lazy { getColumnIndex(Telephony.Mms.STATUS) }
        val mmsTextOnly by lazy { getColumnIndex(Telephony.Mms.TEXT_ONLY) }

        private fun getColumnIndex(columnsName: String): Int {
            return try {
                cursor.getColumnIndexOrThrow(columnsName)
            } catch (e: Exception) {
                Timber.w(e)
                -1
            }
        }
    }
}