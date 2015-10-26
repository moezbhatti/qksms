package com.moez.QKSMS.ui.messagelist;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.util.Log;

public class MessageColumns {

    public static final int COLUMN_MSG_TYPE = 0;
    public static final int COLUMN_ID = 1;
    public static final int COLUMN_THREAD_ID = 2;
    public static final int COLUMN_SMS_ADDRESS = 3;
    public static final int COLUMN_SMS_BODY = 4;
    public static final int COLUMN_SMS_DATE = 5;
    public static final int COLUMN_SMS_DATE_SENT = 6;
    public static final int COLUMN_SMS_READ = 7;
    public static final int COLUMN_SMS_TYPE = 8;
    public static final int COLUMN_SMS_STATUS = 9;
    public static final int COLUMN_SMS_LOCKED = 10;
    public static final int COLUMN_SMS_ERROR_CODE = 11;
    public static final int COLUMN_MMS_SUBJECT = 12;
    public static final int COLUMN_MMS_SUBJECT_CHARSET = 13;
    public static final int COLUMN_MMS_DATE = 14;
    public static final int COLUMN_MMS_DATE_SENT = 15;
    public static final int COLUMN_MMS_READ = 16;
    public static final int COLUMN_MMS_MESSAGE_TYPE = 17;
    public static final int COLUMN_MMS_MESSAGE_BOX = 18;
    public static final int COLUMN_MMS_DELIVERY_REPORT = 19;
    public static final int COLUMN_MMS_READ_REPORT = 20;
    public static final int COLUMN_MMS_ERROR_TYPE = 21;
    public static final int COLUMN_MMS_LOCKED = 22;
    public static final int COLUMN_MMS_STATUS = 23;
    public static final int COLUMN_MMS_TEXT_ONLY = 24;

    public static final int CACHE_SIZE = 50;

    @SuppressLint("InlinedApi")
    public static final String[] PROJECTION = new String[]{
            Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN,
            BaseColumns._ID,
            Telephony.Sms.Conversations.THREAD_ID,
            // For SMS
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.DATE_SENT,
            Telephony.Sms.READ,
            Telephony.Sms.TYPE,
            Telephony.Sms.STATUS,
            Telephony.Sms.LOCKED,
            Telephony.Sms.ERROR_CODE,
            // For MMS
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
            Telephony.Mms.TEXT_ONLY
    };

    public static class ColumnsMap {
        private final String TAG = "ColumnsMap";
        private final boolean DEBUG = false;

        public int mColumnMsgType;
        public int mColumnMsgId;
        public int mColumnSmsAddress;
        public int mColumnSmsBody;
        public int mColumnSmsDate;
        public int mColumnSmsDateSent;
        public int mColumnSmsRead;
        public int mColumnSmsType;
        public int mColumnSmsStatus;
        public int mColumnSmsLocked;
        public int mColumnSmsErrorCode;
        public int mColumnMmsSubject;
        public int mColumnMmsSubjectCharset;
        public int mColumnMmsDate;
        public int mColumnMmsDateSent;
        public int mColumnMmsRead;
        public int mColumnMmsMessageType;
        public int mColumnMmsMessageBox;
        public int mColumnMmsDeliveryReport;
        public int mColumnMmsReadReport;
        public int mColumnMmsErrorType;
        public int mColumnMmsLocked;
        public int mColumnMmsStatus;
        public int mColumnMmsTextOnly;

        public ColumnsMap() {
            mColumnMsgType = COLUMN_MSG_TYPE;
            mColumnMsgId = COLUMN_ID;
            mColumnSmsAddress = COLUMN_SMS_ADDRESS;
            mColumnSmsBody = COLUMN_SMS_BODY;
            mColumnSmsDate = COLUMN_SMS_DATE;
            mColumnSmsDateSent = COLUMN_SMS_DATE_SENT;
            mColumnSmsType = COLUMN_SMS_TYPE;
            mColumnSmsStatus = COLUMN_SMS_STATUS;
            mColumnSmsLocked = COLUMN_SMS_LOCKED;
            mColumnSmsErrorCode = COLUMN_SMS_ERROR_CODE;
            mColumnMmsSubject = COLUMN_MMS_SUBJECT;
            mColumnMmsSubjectCharset = COLUMN_MMS_SUBJECT_CHARSET;
            mColumnMmsMessageType = COLUMN_MMS_MESSAGE_TYPE;
            mColumnMmsMessageBox = COLUMN_MMS_MESSAGE_BOX;
            mColumnMmsDeliveryReport = COLUMN_MMS_DELIVERY_REPORT;
            mColumnMmsReadReport = COLUMN_MMS_READ_REPORT;
            mColumnMmsErrorType = COLUMN_MMS_ERROR_TYPE;
            mColumnMmsLocked = COLUMN_MMS_LOCKED;
            mColumnMmsStatus = COLUMN_MMS_STATUS;
            mColumnMmsTextOnly = COLUMN_MMS_TEXT_ONLY;
        }

        @SuppressLint("InlinedApi")
        public ColumnsMap(Cursor cursor) {
            // Ignore all 'not found' exceptions since the custom columns
            // may be just a subset of the default columns.
            try {
                mColumnMsgType = cursor.getColumnIndexOrThrow(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMsgId = cursor.getColumnIndexOrThrow(BaseColumns._ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnSmsAddress = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnSmsBody = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnSmsDate = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnSmsDateSent = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnSmsType = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnSmsStatus = cursor.getColumnIndexOrThrow(Telephony.Sms.STATUS);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnSmsLocked = cursor.getColumnIndexOrThrow(Telephony.Sms.LOCKED);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnSmsErrorCode = cursor.getColumnIndexOrThrow(Telephony.Sms.ERROR_CODE);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsSubject = cursor.getColumnIndexOrThrow(Telephony.Mms.SUBJECT);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsSubjectCharset = cursor.getColumnIndexOrThrow(Telephony.Mms.SUBJECT_CHARSET);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsMessageType = cursor.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_TYPE);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsMessageBox = cursor.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_BOX);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsDeliveryReport = cursor.getColumnIndexOrThrow(Telephony.Mms.DELIVERY_REPORT);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsReadReport = cursor.getColumnIndexOrThrow(Telephony.Mms.READ_REPORT);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsErrorType = cursor.getColumnIndexOrThrow(Telephony.MmsSms.PendingMessages.ERROR_TYPE);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsLocked = cursor.getColumnIndexOrThrow(Telephony.Mms.LOCKED);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsStatus = cursor.getColumnIndexOrThrow(Telephony.Mms.STATUS);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsTextOnly = cursor.getColumnIndexOrThrow(Telephony.Mms.TEXT_ONLY);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }
        }
    }
}
