package com.moez.QKSMS.data.mapper

import android.database.Cursor
import android.net.Uri
import android.provider.Telephony.Threads
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.ContactRepository
import io.reactivex.Flowable
import javax.inject.Inject

class CursorToConversationFlowable @Inject constructor(val contactsRepo: ContactRepository) : Mapper<Cursor, Flowable<Conversation>> {

    override fun map(from: Cursor): Flowable<Conversation> {
        return from.asFlowable().map { cursor ->
            Conversation().apply {
                id = cursor.getLong(ID)

                cursor.getString(RECIPIENT_IDS).split(" ")
                        .map { id -> id.toLong() }
                        .map { id -> contactsRepo.getContactBlocking(id) }
                        .filter { contact -> contact.recipientId != 0L }
                        .forEach { contact -> contacts.add(contact) }
            }
        }
    }

    companion object {
        val URI: Uri = Uri.parse("content://mms-sms/conversations?simple=true")
        val PROJECTION = arrayOf(
                Threads._ID,
                Threads.RECIPIENT_IDS)

        val ID = 0
        val RECIPIENT_IDS = 1
    }

}