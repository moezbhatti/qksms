package com.moez.QKSMS.data.sync

import android.content.Context
import android.net.Uri
import android.util.Log
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Message
import io.realm.Realm

internal object SyncManager {
    private val TAG = "SyncManager"

    fun copyToRealm(context: Context) {

        val realm = Realm.getDefaultInstance()
        val contentResolver = context.contentResolver
        val conversationsCursor = contentResolver.query(ConversationColumns.URI, ConversationColumns.PROJECTION, null, null, "date desc")

        realm.executeTransaction {
            it.delete(Conversation::class.java)
            it.delete(Message::class.java)

            while (conversationsCursor.moveToNext()) {
                it.insertOrUpdate(Conversation(conversationsCursor))

                val threadId = conversationsCursor.getLong(ConversationColumns.ID)
                val messagesUri = Uri.withAppendedPath(MessageColumns.URI, threadId.toString())
                val messagesCursor = contentResolver.query(messagesUri, MessageColumns.PROJECTION, null, null, "date desc")
                val columnsMap = MessageColumns(messagesCursor)

                Log.i(TAG, "Inserting conversation with ${messagesCursor.count} messages")
                while (messagesCursor.moveToNext()) {
                    val type = messagesCursor.getString(columnsMap.msgType)
                    if (type == "sms" || type == "mms") { // If we can't read the type, don't use this message
                        it.insertOrUpdate(Message(threadId, messagesCursor, columnsMap))
                    }
                }

                messagesCursor.close()
            }
        }

        Log.i(TAG, "Inserted ${conversationsCursor.count} conversations")
        conversationsCursor.close()
        realm.close()
    }

}
