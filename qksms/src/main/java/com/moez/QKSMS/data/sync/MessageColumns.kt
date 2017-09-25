package com.moez.QKSMS.data.sync

import android.database.Cursor
import android.net.Uri
import android.provider.Telephony.*
import timber.log.Timber

@Suppress("unused")
class MessageColumns(val cursor: Cursor) {
    private val TAG = "ColumnsMap"
    private val DEBUG = false

    companion object {
        val URI: Uri = Uri.parse("content://mms-sms/conversations/")
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
