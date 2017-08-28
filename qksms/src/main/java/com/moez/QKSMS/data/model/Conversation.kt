package com.moez.QKSMS.data.model

import android.database.Cursor
import com.moez.QKSMS.data.sync.ConversationColumns
import io.reactivex.Flowable
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Conversation() : RealmObject() {

    @PrimaryKey var id: Long = 0
    var date: Long = 0
    var messageCount: Int = 0
    var recipients: RealmList<Recipient> = RealmList()
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

        Flowable.fromIterable(cursor.getString(ConversationColumns.RECIPIENT_IDS).split(" "))
                .map { id -> id.toLong() }
                .map { id -> Recipient(id) }
                .blockingSubscribe { recipient -> recipients.add(recipient) }

        snippet = cursor.getString(ConversationColumns.SNIPPET) ?: ""
        snippetCs = cursor.getString(ConversationColumns.SNIPPET_CS) ?: ""
        read = cursor.getInt(ConversationColumns.READ)
        error = cursor.getInt(ConversationColumns.ERROR)
        hasAttachment = cursor.getInt(ConversationColumns.HAS_ATTACHMENT)
    }
}
