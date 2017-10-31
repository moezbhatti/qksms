package com.moez.QKSMS.data.mapper

import android.database.Cursor
import android.net.Uri
import android.provider.Telephony.Threads
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.ContactRepository
import io.reactivex.Flowable
import io.reactivex.rxkotlin.toFlowable
import javax.inject.Inject

class CursorToConversationFlowable @Inject constructor(private val contactsRepo: ContactRepository) : Mapper<Cursor, Flowable<Conversation>> {

    override fun map(from: Cursor): Flowable<Conversation> {
        return from.asFlowable().map { cursor ->
            val conversation = Conversation()

            conversation.id = cursor.getLong(ID)

            conversation.contacts.addAll(cursor.getString(RECIPIENT_IDS).split(" ").toFlowable()
                    .map { id -> id.toLong() }
                    .flatMap { id -> contactsRepo.getContact(id) }
                    .filter { contact -> contact.recipientId != 0L }
                    .blockingIterable())

            conversation
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