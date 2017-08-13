package com.moez.QKSMS.model

import android.database.Cursor
import com.moez.QKSMS.data.sync.ConversationColumns
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Conversation() : RealmObject() {

    @PrimaryKey var id: Long = 0
    var date: Long = 0
    var messageCount: Int = 0
    var recipientIds: Int = 0
    var snippet: String = ""
    var snippetCs: String = ""
    var read: Int = 0
    var error: Int = 0
    var hasAttachment: Int = 0

    /**
     * Instantiates a Conversation from the Android SMS ContentProvider
     */
    constructor(cursor: Cursor) : this() {
        id = cursor.getLong(ConversationColumns.ID)
        date = cursor.getLong(ConversationColumns.DATE)
        messageCount = cursor.getInt(ConversationColumns.MESSAGE_COUNT)
        recipientIds = cursor.getInt(ConversationColumns.RECIPIENT_IDS)
        snippet = cursor.getString(ConversationColumns.SNIPPET) ?: ""
        snippetCs = cursor.getString(ConversationColumns.SNIPPET_CS) ?: ""
        read = cursor.getInt(ConversationColumns.READ)
        error = cursor.getInt(ConversationColumns.ERROR)
        hasAttachment = cursor.getInt(ConversationColumns.HAS_ATTACHMENT)
    }

}
