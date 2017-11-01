package com.moez.QKSMS.data.mapper

import android.content.Context
import android.database.Cursor
import android.provider.Telephony.*
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.model.Message
import io.reactivex.Flowable
import timber.log.Timber
import javax.inject.Inject

class CursorToMessageFlowable @Inject constructor(val context: Context) : Mapper<Cursor, Flowable<Message>> {

    override fun map(from: Cursor): Flowable<Message> {
        val columnsMap = MessageColumns(from)
        return from.asFlowable().map { cursor ->
            Message().apply {
                type = when (cursor.getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN)) {
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
                        address = getMmsAddress(id)
                        boxId = cursor.getInt(columnsMap.mmsMessageBox)
                        date = cursor.getLong(columnsMap.mmsDate) * 1000L
                        dateSent = cursor.getLong(columnsMap.mmsDateSent)
                        seen = cursor.getInt(columnsMap.mmsSeen) != 0
                        read = cursor.getInt(columnsMap.mmsRead) != 0
                        errorType = cursor.getInt(columnsMap.mmsErrorType)
                    }
                }
            }
        }
    }

    private fun getMmsAddress(messageId: Long): String {
        val uri = Mms.CONTENT_URI.buildUpon().appendPath(messageId.toString()).appendPath("addr").build()
        val projection = arrayOf(Mms.Addr.ADDRESS, Mms.Addr.CHARSET)
        val selection = "${Mms.Addr.TYPE} = 137"

        context.contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return ""
    }

    companion object {
        val CURSOR_PROJECTION = arrayOf(
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

    private class MessageColumns(val cursor: Cursor) {

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