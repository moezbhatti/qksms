package com.moez.QKSMS.data.mapper

import android.database.Cursor
import android.net.Uri
import android.provider.Telephony.Threads
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Recipient
import javax.inject.Inject

class CursorToConversation @Inject constructor() : Mapper<Cursor, Conversation> {

    companion object {
        val URI: Uri = Uri.parse("content://mms-sms/conversations?simple=true")
        val PROJECTION = arrayOf(
                Threads._ID,
                Threads.RECIPIENT_IDS)

        val ID = 0
        val RECIPIENT_IDS = 1
    }

    override fun map(from: Cursor): Conversation {
        return Conversation().apply {
            id = from.getLong(ID)
            recipients.addAll(from.getString(RECIPIENT_IDS)
                    .split(" ")
                    .map { recipientId -> recipientId.toLong() }
                    .map { recipientId -> Recipient().apply { id = recipientId } })
        }
    }

}