package com.moez.QKSMS.data.sync

import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.Telephony
import android.util.Log

@Suppress("unused")
class MessageColumns(val cursor: Cursor) {
    private val TAG = "ColumnsMap"
    private val DEBUG = false

    companion object {
        val URI: Uri = Uri.parse("content://mms-sms/conversations/")
        val PROJECTION = arrayOf(
                Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN,
                BaseColumns._ID,
                Telephony.Sms.Conversations.THREAD_ID,

                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.DATE_SENT,
                Telephony.Sms.READ,
                Telephony.Sms.TYPE,
                Telephony.Sms.STATUS,
                Telephony.Sms.LOCKED,
                Telephony.Sms.ERROR_CODE,

                Telephony.Mms.SUBJECT,
                Telephony.Mms.SUBJECT_CHARSET,
                Telephony.Mms.DATE,
                Telephony.Mms.DATE_SENT,
                Telephony.Mms.READ,
                Telephony.Mms.MESSAGE_TYPE,
                Telephony.Mms.MESSAGE_BOX,
                Telephony.Mms.DELIVERY_REPORT,
                Telephony.Mms.READ_REPORT,
                Telephony.MmsSms.PendingMessages.ERROR_TYPE,
                Telephony.Mms.LOCKED,
                Telephony.Mms.STATUS,
                Telephony.Mms.TEXT_ONLY)
    }

    val msgType = getColumnIndex(0)
    val msgId = getColumnIndex(1)
    val threadId = getColumnIndex(2)

    val smsAddress = getColumnIndex(3)
    val smsBody = getColumnIndex(4)
    val smsDate = getColumnIndex(5)
    val smsDateSent = getColumnIndex(6)
    val smsRead = getColumnIndex(7)
    val smsType = getColumnIndex(8)
    val smsStatus = getColumnIndex(9)
    val smsLocked = getColumnIndex(10)
    val smsErrorCode = getColumnIndex(11)

    val mmsSubject = getColumnIndex(12)
    val mmsSubjectCharset = getColumnIndex(13)
    val mmsDate = getColumnIndex(14)
    val mmsDateSent = getColumnIndex(15)
    val mmsRead = getColumnIndex(16)
    val mmsMessageType = getColumnIndex(17)
    val mmsMessageBox = getColumnIndex(18)
    val mmsDeliveryReport = getColumnIndex(19)
    val mmsReadReport = getColumnIndex(20)
    val mmsErrorType = getColumnIndex(21)
    val mmsLocked = getColumnIndex(22)
    val mmsStatus = getColumnIndex(23)
    val mmsTextOnly = getColumnIndex(24)

    private fun getColumnIndex(index: Int): Int {
        try {
            return cursor.getColumnIndexOrThrow(PROJECTION[index])
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        return index
    }

}
