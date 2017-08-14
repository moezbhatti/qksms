package com.moez.QKSMS.model

import android.database.Cursor
import android.provider.Telephony
import android.provider.Telephony.Mms
import android.provider.Telephony.Sms
import com.moez.QKSMS.data.sync.MessageColumns
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Message() : RealmObject() {

    @PrimaryKey var id: Long = 0
    var threadId: Long = 0
    var boxId: Int = 0
    var type: String = ""
    var body: String = ""
    var date: Long = 0
    var dateSent: Long = 0
    var errorType: Int = 0

    constructor(threadId: Long, cursor: Cursor, columnsMap: MessageColumns) : this() {
        this.threadId = threadId

        id = cursor.getLong(columnsMap.msgId)
        type = cursor.getString(columnsMap.msgType)

        val isMms = type == "mms"
        body = cursor.getString(columnsMap.smsBody) ?: ""
        errorType = cursor.getInt(columnsMap.mmsErrorType)

        if (isMms) {
            boxId = cursor.getInt(columnsMap.mmsMessageBox)
            date = cursor.getLong(columnsMap.smsDate)
            dateSent = cursor.getLong(columnsMap.smsDateSent)
        } else {
            boxId = cursor.getInt(columnsMap.smsType)
            date = cursor.getLong(columnsMap.mmsDate)
            dateSent = cursor.getLong(columnsMap.mmsDateSent)
        }
    }

    fun isMms(): Boolean = type == "mms"

    fun isSms(): Boolean = type == "sms"

    fun isMe(): Boolean {
        val isIncomingMms = isMms() && (boxId == Mms.MESSAGE_BOX_INBOX || boxId == Mms.MESSAGE_BOX_ALL)
        val isIncomingSms = isSms() && (boxId == Sms.MESSAGE_TYPE_INBOX || boxId == Sms.MESSAGE_TYPE_ALL)

        return !(isIncomingMms || isIncomingSms)
    }

    fun isOutgoingMessage(): Boolean {
        val isOutgoingMms = isMms() && boxId == Mms.MESSAGE_BOX_OUTBOX
        val isOutgoingSms = isSms() && (boxId == Sms.MESSAGE_TYPE_FAILED
                || boxId == Sms.MESSAGE_TYPE_OUTBOX
                || boxId == Sms.MESSAGE_TYPE_QUEUED)

        return isOutgoingMms || isOutgoingSms
    }

    fun isSending(): Boolean {
        return !isFailedMessage() && isOutgoingMessage()
    }

    fun isFailedMessage(): Boolean {
        val isFailedMms = isMms() && errorType >= Telephony.MmsSms.ERR_TYPE_GENERIC_PERMANENT
        val isFailedSms = isSms() && boxId == Sms.MESSAGE_TYPE_FAILED
        return isFailedMms || isFailedSms
    }

}
