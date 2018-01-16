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
package common.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import common.util.extensions.asFlowable
import common.util.extensions.insertOrUpdate
import common.util.extensions.map
import common.util.extensions.mapWhile
import common.util.filter.ContactFilter
import data.mapper.CursorToContact
import data.mapper.CursorToConversation
import data.mapper.CursorToMessage
import data.mapper.CursorToRecipient
import data.model.*
import data.repository.MessageRepository
import io.reactivex.Flowable
import io.realm.Realm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
        private val context: Context,
        private val messageRepo: MessageRepository,
        private val cursorToConversation: CursorToConversation,
        private val cursorToMessage: CursorToMessage,
        private val cursorToRecipient: CursorToRecipient,
        private val cursorToContact: CursorToContact,
        private val contactFilter: ContactFilter) {

    sealed class Status {
        class Idle : Status()
        class Running : Status()
    }

    private val contentResolver = context.contentResolver
    private var status: Status = Status.Idle()

    // TODO: This needs to be substantially faster
    fun syncMessages(fullSync: Boolean = false) {

        // If the sync is already running, don't try to do another one
        if (status is Status.Running) return
        status = Status.Running()

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        if (fullSync) {
            realm.delete(Conversation::class.java)
            realm.delete(Message::class.java)
            realm.delete(MmsPart::class.java)
            realm.delete(Recipient::class.java)
            realm.delete(SyncLog::class.java)
        }

        val lastSync = realm.where(Message::class.java)?.max("date")?.toLong() ?: 0
        realm.insert(SyncLog())

        // Sync conversations
        val conversationCursor = contentResolver.query(
                CursorToConversation.URI,
                CursorToConversation.PROJECTION,
                "date > ?", arrayOf(lastSync.toString()),
                "date desc")
        val conversations = conversationCursor.map { cursor -> cursorToConversation.map(cursor) }
        realm.insertOrUpdate(conversations)
        conversationCursor.close()


        // Sync messages
        val messageCursor = contentResolver.query(CursorToMessage.URI, CursorToMessage.PROJECTION, null, null, "normalized_date desc")
        val messageColumns = CursorToMessage.MessageColumns(messageCursor)
        val messages = messageCursor.mapWhile(
                { cursor -> cursorToMessage.map(Pair(cursor, messageColumns)) },
                { message -> message.date > lastSync })
        realm.insertOrUpdate(messages)
        messageCursor.close()


        // Sync recipients
        val recipientCursor = contentResolver.query(CursorToRecipient.URI, null, null, null, null)
        val recipients = recipientCursor.map { cursor -> cursorToRecipient.map(cursor) }
        realm.insertOrUpdate(recipients)
        recipientCursor.close()

        realm.commitTransaction()
        realm.close()

        syncContacts()

        status = Status.Idle()
    }

    fun syncMessage(uri: Uri): Flowable<Message> {
        val id = ContentUris.parseId(uri)
        val type = when {
            uri.toString().contains("mms") -> "mms"
            uri.toString().contains("sms") -> "sms"
            else -> return Flowable.empty()
        }

        // Check if the message already exists, so we can reuse the id
        val existingId = Realm.getDefaultInstance().use { realm ->
            realm.refresh()
            realm.where(Message::class.java)
                    .equalTo("type", type)
                    .equalTo("contentId", id)
                    .findFirst()
                    ?.id
        }

        // The uri might be something like content://mms/inbox/id
        // The box might change though, so we should just use the mms/id uri
        val stableUri = when (type) {
            "mms" -> ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, id)
            else -> ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, id)
        }

        val cursor = context.contentResolver.query(stableUri, null, null, null, null)
        val columnsMap = CursorToMessage.MessageColumns(cursor)

        // Map the cursor to a message
        return cursor.asFlowable()
                .map { cursorToMessage.map(Pair(it, columnsMap)) }
                .map { message ->
                    existingId?.let { message.id = it }
                    message
                }
                .doOnNext { message -> messageRepo.getOrCreateConversation(message.threadId) }
                .doOnNext { message -> message.insertOrUpdate() }
    }

    fun syncContacts() {
        // Load all the contacts
        var contacts = context.contentResolver.query(CursorToContact.URI, CursorToContact.PROJECTION, null, null, null)
                .map { cursor -> cursorToContact.map(cursor) }
                .groupBy { contact -> contact.lookupKey }
                .map { contacts ->
                    val allNumbers = contacts.value.map { it.numbers }.flatten()
                    contacts.value.first().apply {
                        numbers.clear()
                        numbers.addAll(allNumbers)
                    }
                }

        val realm = Realm.getDefaultInstance()
        val recipients = realm.where(Recipient::class.java).findAll()

        realm.executeTransaction {
            realm.delete(Contact::class.java)

            contacts = realm.copyToRealm(contacts)

            // Update all the recipients with the new contacts
            val updatedRecipients = recipients.map { recipient ->
                recipient.apply { contact = contacts.firstOrNull { contactFilter.filter(it, recipient.address) } }
            }

            realm.insertOrUpdate(updatedRecipients)
        }
        realm.close()
    }

}