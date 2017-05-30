package com.moez.QKSMS.data.sync

import android.util.Log
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_ID
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_MMS_DELIVERY_REPORT
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_MMS_ERROR_TYPE
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_MMS_LOCKED
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_MMS_MESSAGE_BOX
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_MMS_MESSAGE_TYPE
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_MMS_READ_REPORT
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_MMS_STATUS
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_MMS_SUBJECT
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_MMS_SUBJECT_CHARSET
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_MMS_TEXT_ONLY
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_MSG_TYPE
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_SMS_ADDRESS
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_SMS_BODY
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_SMS_DATE
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_SMS_DATE_SENT
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_SMS_ERROR_CODE
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_SMS_LOCKED
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_SMS_STATUS
import com.moez.QKSMS.data.sync.MessageSyncManager.COLUMN_SMS_TYPE

class ColumnsMap {
    private val TAG = "ColumnsMap"
    private val DEBUG = false

    var mColumnMsgType: Int = 0
    var mColumnMsgId: Int = 0
    var mColumnSmsAddress: Int = 0
    var mColumnSmsBody: Int = 0
    var mColumnSmsDate: Int = 0
    var mColumnSmsDateSent: Int = 0
    var mColumnSmsRead: Int = 0
    var mColumnSmsType: Int = 0
    var mColumnSmsStatus: Int = 0
    var mColumnSmsLocked: Int = 0
    var mColumnSmsErrorCode: Int = 0
    var mColumnMmsSubject: Int = 0
    var mColumnMmsSubjectCharset: Int = 0
    var mColumnMmsDate: Int = 0
    var mColumnMmsDateSent: Int = 0
    var mColumnMmsRead: Int = 0
    var mColumnMmsMessageType: Int = 0
    var mColumnMmsMessageBox: Int = 0
    var mColumnMmsDeliveryReport: Int = 0
    var mColumnMmsReadReport: Int = 0
    var mColumnMmsErrorType: Int = 0
    var mColumnMmsLocked: Int = 0
    var mColumnMmsStatus: Int = 0
    var mColumnMmsTextOnly: Int = 0

    constructor() {
        mColumnMsgType = COLUMN_MSG_TYPE
        mColumnMsgId = COLUMN_ID
        mColumnSmsAddress = COLUMN_SMS_ADDRESS
        mColumnSmsBody = COLUMN_SMS_BODY
        mColumnSmsDate = COLUMN_SMS_DATE
        mColumnSmsDateSent = COLUMN_SMS_DATE_SENT
        mColumnSmsType = COLUMN_SMS_TYPE
        mColumnSmsStatus = COLUMN_SMS_STATUS
        mColumnSmsLocked = COLUMN_SMS_LOCKED
        mColumnSmsErrorCode = COLUMN_SMS_ERROR_CODE
        mColumnMmsSubject = COLUMN_MMS_SUBJECT
        mColumnMmsSubjectCharset = COLUMN_MMS_SUBJECT_CHARSET
        mColumnMmsMessageType = COLUMN_MMS_MESSAGE_TYPE
        mColumnMmsMessageBox = COLUMN_MMS_MESSAGE_BOX
        mColumnMmsDeliveryReport = COLUMN_MMS_DELIVERY_REPORT
        mColumnMmsReadReport = COLUMN_MMS_READ_REPORT
        mColumnMmsErrorType = COLUMN_MMS_ERROR_TYPE
        mColumnMmsLocked = COLUMN_MMS_LOCKED
        mColumnMmsStatus = COLUMN_MMS_STATUS
        mColumnMmsTextOnly = COLUMN_MMS_TEXT_ONLY
    }

    @android.annotation.SuppressLint("InlinedApi")
    constructor(cursor: android.database.Cursor) {
        // Ignore all 'not found' exceptions since the custom columns
        // may be just a subset of the default columns.
        try {
            mColumnMsgType = cursor.getColumnIndexOrThrow(android.provider.Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnMsgId = cursor.getColumnIndexOrThrow(android.provider.BaseColumns._ID)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnSmsAddress = cursor.getColumnIndexOrThrow(android.provider.Telephony.Sms.ADDRESS)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnSmsBody = cursor.getColumnIndexOrThrow(android.provider.Telephony.Sms.BODY)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnSmsDate = cursor.getColumnIndexOrThrow(android.provider.Telephony.Sms.DATE)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnSmsDateSent = cursor.getColumnIndexOrThrow(android.provider.Telephony.Sms.DATE_SENT)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnSmsType = cursor.getColumnIndexOrThrow(android.provider.Telephony.Sms.TYPE)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnSmsStatus = cursor.getColumnIndexOrThrow(android.provider.Telephony.Sms.STATUS)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnSmsLocked = cursor.getColumnIndexOrThrow(android.provider.Telephony.Sms.LOCKED)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnSmsErrorCode = cursor.getColumnIndexOrThrow(android.provider.Telephony.Sms.ERROR_CODE)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnMmsSubject = cursor.getColumnIndexOrThrow(android.provider.Telephony.Mms.SUBJECT)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnMmsSubjectCharset = cursor.getColumnIndexOrThrow(android.provider.Telephony.Mms.SUBJECT_CHARSET)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnMmsMessageType = cursor.getColumnIndexOrThrow(android.provider.Telephony.Mms.MESSAGE_TYPE)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnMmsMessageBox = cursor.getColumnIndexOrThrow(android.provider.Telephony.Mms.MESSAGE_BOX)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnMmsDeliveryReport = cursor.getColumnIndexOrThrow(android.provider.Telephony.Mms.DELIVERY_REPORT)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnMmsReadReport = cursor.getColumnIndexOrThrow(android.provider.Telephony.Mms.READ_REPORT)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnMmsErrorType = cursor.getColumnIndexOrThrow(android.provider.Telephony.MmsSms.PendingMessages.ERROR_TYPE)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnMmsLocked = cursor.getColumnIndexOrThrow(android.provider.Telephony.Mms.LOCKED)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnMmsStatus = cursor.getColumnIndexOrThrow(android.provider.Telephony.Mms.STATUS)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

        try {
            mColumnMmsTextOnly = cursor.getColumnIndexOrThrow(android.provider.Telephony.Mms.TEXT_ONLY)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.w(TAG, e.message)
        }

    }
}
