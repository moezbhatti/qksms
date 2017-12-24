package com.moez.QKSMS.common.util

import android.content.Context
import com.moez.QKSMS.common.util.extensions.map
import com.moez.QKSMS.common.util.extensions.mapWhile
import com.moez.QKSMS.data.mapper.CursorToConversation
import com.moez.QKSMS.data.mapper.CursorToMessage
import com.moez.QKSMS.data.mapper.CursorToRecipient
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.model.Recipient
import com.moez.QKSMS.data.model.SyncLog
import io.realm.Realm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
        context: Context,
        private val cursorToConversation: CursorToConversation,
        private val cursorToMessage: CursorToMessage,
        private val cursorToRecipient: CursorToRecipient) {

    sealed class Status {
        class Idle : Status()
        class Running(progress: Int = 0) : Status()
    }

    private val contentResolver = context.contentResolver
    private var status: Status = Status.Idle()

    // TODO: This needs to be substantially faster
    fun performSync(fullSync: Boolean = false) {

        // If the sync is already running, don't try to do another one
        if (status is Status.Running) return
        status = Status.Running()

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        if (fullSync) {
            realm.delete(Conversation::class.java)
            realm.delete(Message::class.java)
            realm.delete(Recipient::class.java)
            realm.delete(SyncLog::class.java)
        }

        val lastSync = realm.where(SyncLog::class.java)?.max("date")?.toLong() ?: 0
        realm.insert(SyncLog())

        // Sync conversations
        val conversationCursor = contentResolver.query(
                CursorToConversation.URI,
                CursorToConversation.PROJECTION,
                "date >= ?", arrayOf(lastSync.toString()),
                "date desc")
        val conversations = conversationCursor.map { cursor -> cursorToConversation.map(cursor) }
        realm.insertOrUpdate(conversations)
        conversationCursor.close()


        // Sync messages
        val messageCursor = contentResolver.query(CursorToMessage.URI, CursorToMessage.PROJECTION, null, null, "normalized_date desc")
        val messageColumns = CursorToMessage.MessageColumns(messageCursor)
        val messages = messageCursor.mapWhile(
                { cursor -> cursorToMessage.map(Pair(cursor, messageColumns)) },
                { message -> message.date >= lastSync })
        realm.insertOrUpdate(messages)
        messageCursor.close()


        // Sync recipients
        val recipientCursor = contentResolver.query(CursorToRecipient.URI, null, null, null, null)
        val recipients = recipientCursor.map { cursor -> cursorToRecipient.map(cursor) }
        realm.insertOrUpdate(recipients)
        recipientCursor.close()


        realm.commitTransaction()
        realm.close()

        status = Status.Idle()
    }

}