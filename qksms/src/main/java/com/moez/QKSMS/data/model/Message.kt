package com.moez.QKSMS.data.model

import android.provider.Telephony
import android.provider.Telephony.Mms
import android.provider.Telephony.Sms
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Message : RealmObject() {

    @PrimaryKey var id: Long = 0
    var threadId: Long = 0
    var boxId: Int = 0
    var type: String = ""
    var body: String = ""
    var date: Long = 0
    var dateSent: Long = 0
    var seen: Boolean = false
    var read: Boolean = false
    var errorType: Int = 0

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
