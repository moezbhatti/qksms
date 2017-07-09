package com.moez.QKSMS.data.sync

import android.database.Cursor
import android.util.Log

@Suppress("unused")
class ColumnsMap(val cursor: Cursor) {
    private val TAG = "ColumnsMap"
    private val DEBUG = false

    var msgType = getColumnIndex(MessageSyncManager.COLUMN_MSG_TYPE)
    var msgId = getColumnIndex(MessageSyncManager.COLUMN_ID)
    var threadId = getColumnIndex(MessageSyncManager.COLUMN_THREAD_ID)
    var smsAddress = getColumnIndex(MessageSyncManager.COLUMN_SMS_ADDRESS)
    var smsBody = getColumnIndex(MessageSyncManager.COLUMN_SMS_BODY)
    var smsDate = getColumnIndex(MessageSyncManager.COLUMN_SMS_DATE)
    var smsDateSent = getColumnIndex(MessageSyncManager.COLUMN_SMS_DATE_SENT)
    var smsRead = getColumnIndex(MessageSyncManager.COLUMN_SMS_READ)
    var smsType = getColumnIndex(MessageSyncManager.COLUMN_SMS_TYPE)
    var smsStatus = getColumnIndex(MessageSyncManager.COLUMN_SMS_STATUS)
    var smsLocked = getColumnIndex(MessageSyncManager.COLUMN_SMS_LOCKED)
    var smsErrorCode = getColumnIndex(MessageSyncManager.COLUMN_SMS_ERROR_CODE)
    var mmsSubject = getColumnIndex(MessageSyncManager.COLUMN_MMS_SUBJECT)
    var mmsSubjectCharset = getColumnIndex(MessageSyncManager.COLUMN_MMS_SUBJECT_CHARSET)
    var mmsDate = getColumnIndex(MessageSyncManager.COLUMN_MMS_DATE)
    var mmsDateSent = getColumnIndex(MessageSyncManager.COLUMN_MMS_DATE_SENT)
    var mmsRead = getColumnIndex(MessageSyncManager.COLUMN_MMS_READ)
    var mmsMessageType = getColumnIndex(MessageSyncManager.COLUMN_MMS_MESSAGE_TYPE)
    var mmsMessageBox = getColumnIndex(MessageSyncManager.COLUMN_MMS_MESSAGE_BOX)
    var mmsDeliveryReport = getColumnIndex(MessageSyncManager.COLUMN_MMS_DELIVERY_REPORT)
    var mmsReadReport = getColumnIndex(MessageSyncManager.COLUMN_MMS_READ_REPORT)
    var mmsErrorType = getColumnIndex(MessageSyncManager.COLUMN_MMS_ERROR_TYPE)
    var mmsLocked = getColumnIndex(MessageSyncManager.COLUMN_MMS_LOCKED)
    var mmsStatus = getColumnIndex(MessageSyncManager.COLUMN_MMS_STATUS)
    var mmsTextOnly = getColumnIndex(MessageSyncManager.COLUMN_MMS_TEXT_ONLY)

    private fun getColumnIndex(index: Int): Int {
        try {
            return cursor.getColumnIndexOrThrow(MessageSyncManager.PROJECTION[index])
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        return index
    }

}
