package com.moez.QKSMS.data.sync

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.Telephony
import com.moez.QKSMS.model.Message
import io.realm.Realm


internal object MessageSyncManager {

    val COLUMN_MSG_TYPE = 0
    val COLUMN_ID = 1
    val COLUMN_THREAD_ID = 2
    val COLUMN_SMS_ADDRESS = 3
    val COLUMN_SMS_BODY = 4
    val COLUMN_SMS_DATE = 5
    val COLUMN_SMS_DATE_SENT = 6
    val COLUMN_SMS_READ = 7
    val COLUMN_SMS_TYPE = 8
    val COLUMN_SMS_STATUS = 9
    val COLUMN_SMS_LOCKED = 10
    val COLUMN_SMS_ERROR_CODE = 11
    val COLUMN_MMS_SUBJECT = 12
    val COLUMN_MMS_SUBJECT_CHARSET = 13
    val COLUMN_MMS_DATE = 14
    val COLUMN_MMS_DATE_SENT = 15
    val COLUMN_MMS_READ = 16
    val COLUMN_MMS_MESSAGE_TYPE = 17
    val COLUMN_MMS_MESSAGE_BOX = 18
    val COLUMN_MMS_DELIVERY_REPORT = 19
    val COLUMN_MMS_READ_REPORT = 20
    val COLUMN_MMS_ERROR_TYPE = 21
    val COLUMN_MMS_LOCKED = 22
    val COLUMN_MMS_STATUS = 23
    val COLUMN_MMS_TEXT_ONLY = 24

    val MMS_SMS_CONTENT_PROVIDER = Uri.parse("content://mms-sms/conversations/")

    @SuppressLint("InlinedApi")
    val PROJECTION = arrayOf(
            Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN,
            BaseColumns._ID,
            Telephony.Sms.Conversations.THREAD_ID,

            // For SMS
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.DATE_SENT,
            Telephony.Sms.READ, Telephony.Sms.TYPE,
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
            Telephony.Mms.TEXT_ONLY)

    fun copyToRealm(context: Context, threadId: Long) {
        val cursor = context.contentResolver.query(Uri.withAppendedPath(MMS_SMS_CONTENT_PROVIDER, threadId.toString()), PROJECTION, null, null, "date desc")
        val columnsMap = ColumnsMap(cursor)

        Realm.getDefaultInstance().executeTransaction {
            while (cursor.moveToNext()) {

                val type = cursor.getString(columnsMap.mColumnMsgType)
                if (type != "sms" && type != "mms") continue // If we can't read the type, don't use this message

                val isMms = type == "mms"
                val body = cursor.getString(columnsMap.mColumnSmsBody) ?: ""
                val errorType = cursor.getInt(columnsMap.mColumnMmsErrorType)

                val boxId: Int

                if (isMms) {
                    boxId = cursor.getInt(columnsMap.mColumnMmsMessageBox)
                } else {
                    boxId = cursor.getInt(columnsMap.mColumnSmsType)
                }

                it.insertOrUpdate(
                        Message(cursor.getLong(columnsMap.mColumnMsgId), threadId, boxId, type, body, errorType))
            }
        }

        cursor.close()
    }

}