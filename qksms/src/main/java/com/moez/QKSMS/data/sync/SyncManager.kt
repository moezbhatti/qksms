package com.moez.QKSMS.data.sync

import android.content.Context
import android.net.Uri
import android.util.Log
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.util.forEach
import io.realm.Realm

internal object SyncManager {
    private val TAG = "SyncManager"

    fun copyToRealm(context: Context) {

        val contentResolver = context.contentResolver
        val conversationsCursor = contentResolver.query(ConversationColumns.URI, ConversationColumns.PROJECTION, null, null, "date desc")

        // Get the total number of messages so that we can calculate progress
        var totalMessages = 0
        var currentMessage = 0
        conversationsCursor.forEach(false) { totalMessages += it.getInt(ConversationColumns.MESSAGE_COUNT) }


        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            it.delete(Conversation::class.java)
            it.delete(Message::class.java)

            conversationsCursor.forEach {
                realm.insertOrUpdate(Conversation(conversationsCursor))

                val threadId = conversationsCursor.getLong(ConversationColumns.ID)
                val messagesUri = Uri.withAppendedPath(MessageColumns.URI, threadId.toString())
                val messagesCursor = contentResolver.query(messagesUri, MessageColumns.PROJECTION, null, null, "date desc")
                val columnsMap = MessageColumns(messagesCursor)

                messagesCursor.forEach {
                    currentMessage++
                    val type = messagesCursor.getString(columnsMap.msgType)
                    if (type == "sms" || type == "mms") { // If we can't read the type, don't use this message
                        realm.insertOrUpdate(Message(threadId, messagesCursor, columnsMap))
                    }
                }
            }
        }
        realm.close()

        Log.i(TAG, "Inserted $currentMessage messages")
    }

}
