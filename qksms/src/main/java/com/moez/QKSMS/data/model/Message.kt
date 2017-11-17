package com.moez.QKSMS.data.model

import android.content.ContentUris
import android.net.Uri
import android.provider.Telephony.*
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

open class Message : RealmObject() {

    enum class DeliveryStatus { NONE, INFO, FAILED, PENDING, RECEIVED }
    enum class AttachmentType { TEXT, IMAGE, VIDEO, AUDIO, SLIDESHOW, NOT_LOADED }

    @PrimaryKey var id: Long = 0
    var threadId: Long = 0
    var address: String = ""
    var boxId: Int = 0
    var type: String = ""
    var body: String = ""
    var date: Long = 0
    var dateSent: Long = 0
    var seen: Boolean = false
    var read: Boolean = false
    var locked: Boolean = false

    @Ignore var deliveryStatus: DeliveryStatus = DeliveryStatus.NONE
        get() = DeliveryStatus.valueOf(deliveryStatusString)
    var deliveryStatusString: String = "NONE"
        get() = deliveryStatus.toString()

    // SMS only
    var errorCode: Int = 0

    // MMS only
    @Ignore var attachmentType: AttachmentType = AttachmentType.NOT_LOADED
        get() = AttachmentType.valueOf(attachmentTypeString)
    var attachmentTypeString: String = "NOT_LOADED"
        get() = attachmentType.toString()

    var mmsDeliveryStatusString: String = ""
    var readReportString: String = ""
    var errorType: Int = 0
    var messageSize: Int = 0
    var messageType: Int = 0
    var mmsStatus: Int = 0
    var subject: String = ""
    var textContentType: String = ""

    fun getUri(): Uri {
        val baseUri = if (isMms()) Mms.CONTENT_URI else Sms.CONTENT_URI
        return ContentUris.withAppendedId(baseUri, id)
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
        val isFailedMms = isMms() && errorType >= MmsSms.ERR_TYPE_GENERIC_PERMANENT
        val isFailedSms = isSms() && boxId == Sms.MESSAGE_TYPE_FAILED
        return isFailedMms || isFailedSms
    }
}