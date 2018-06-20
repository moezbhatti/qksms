/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.model

import android.content.ContentUris
import android.net.Uri
import android.provider.Telephony.*
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class Message : RealmObject() {

    enum class AttachmentType { TEXT, IMAGE, VIDEO, AUDIO, SLIDESHOW, NOT_LOADED }

    @PrimaryKey var id: Long = 0

    @Index var threadId: Long = 0

    // The MMS-SMS content provider returns messages where duplicate ids can exist. This is because
    // SMS and MMS are stored in separate tables. We can't use these ids as our realm message id
    // since it's our primary key for the single message object, so we'll store the original id in
    // case we need to access the original message item in the content provider
    var contentId: Long = 0
    var address: String = ""
    var boxId: Int = 0
    var type: String = ""
    var date: Long = 0
    var dateSent: Long = 0
    var seen: Boolean = false
    var read: Boolean = false
    var locked: Boolean = false
    var subId: Int = -1

    // SMS only
    var body: String = ""
    var errorCode: Int = 0
    var deliveryStatus: Int = Sms.STATUS_NONE

    // MMS only
    var attachmentTypeString: String = AttachmentType.NOT_LOADED.toString()
    var attachmentType: AttachmentType
        get() = AttachmentType.valueOf(attachmentTypeString)
        set(value) {
            attachmentTypeString = value.toString()
        }

    var mmsDeliveryStatusString: String = ""
    var readReportString: String = ""
    var errorType: Int = 0
    var messageSize: Int = 0
    var messageType: Int = 0
    var mmsStatus: Int = 0
    var subject: String = ""
    var textContentType: String = ""
    var parts: RealmList<MmsPart> = RealmList()

    fun getUri(): Uri {
        val baseUri = if (isMms()) Mms.CONTENT_URI else Sms.CONTENT_URI
        return ContentUris.withAppendedId(baseUri, contentId)
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

    /**
     * Returns the text that should be copied to the clipboard
     */
    fun getText(): String {
        return when {
            isSms() -> body

            else -> parts
                    .mapNotNull { it.text }
                    .joinToString("\n") { text -> text }
        }
    }

    /**
     * Returns the text that should be displayed when a preview of the message
     * needs to be displayed, such as in the conversation view or in a notification
     *
     * TODO: Don't return a hardcoded string
     */
    fun getSummary(): String {
        return when {
            isSms() -> body

            else -> parts
                    .mapNotNull { it.text }
                    .joinToString("\n") { text -> text }
                    .takeIf { it.isNotEmpty() }
                    ?: getCleansedSubject().takeIf { it.isNotEmpty() }
                    ?: "An MMS message"
        }
    }

    /**
     * Cleanses the subject in case it's useless, so that the UI doesn't have to show it
     */
    fun getCleansedSubject(): String {
        val uselessSubjects = listOf("no subject", "NoSubject", "<not present>")

        return if (uselessSubjects.contains(subject)) "" else subject
    }

    fun isSending(): Boolean {
        return !isFailedMessage() && isOutgoingMessage()
    }

    fun isDelivered(): Boolean {
        val isDeliveredMms = false // TODO
        val isDeliveredSms = deliveryStatus == Sms.STATUS_COMPLETE
        return isDeliveredMms || isDeliveredSms
    }

    fun isFailedMessage(): Boolean {
        val isFailedMms = isMms() && (errorType >= MmsSms.ERR_TYPE_GENERIC_PERMANENT || boxId == Mms.MESSAGE_BOX_FAILED)
        val isFailedSms = isSms() && boxId == Sms.MESSAGE_TYPE_FAILED
        return isFailedMms || isFailedSms
    }

    fun compareSender(other: Message): Boolean = when {
        isMe() && other.isMe() -> subId == other.subId
        !isMe() && !other.isMe() -> subId == other.subId && address == other.address
        else -> false
    }
}