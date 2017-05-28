package com.moez.QKSMS.util


import android.content.Context
import android.net.Uri
import android.provider.Telephony.Threads
import com.moez.QKSMS.model.Conversation
import io.realm.Realm

internal object SyncManager {
    val TAG = "SyncManager"

    val CONVERSATIONS_CONTENT_PROVIDER = Uri.parse("content://mms-sms/conversations?simple=true")
    val ALL_THREADS_PROJECTION = arrayOf(
            Threads._ID,
            Threads.DATE,
            Threads.MESSAGE_COUNT,
            Threads.RECIPIENT_IDS,
            Threads.SNIPPET,
            Threads.SNIPPET_CHARSET,
            Threads.READ,
            Threads.ERROR,
            Threads.HAS_ATTACHMENT)

    val ID = 0
    val DATE = 1
    val MESSAGE_COUNT = 2
    val RECIPIENT_IDS = 3
    val SNIPPET = 4
    val SNIPPET_CS = 5
    val READ = 6
    val ERROR = 7
    val HAS_ATTACHMENT = 8

    fun dumpColumns(context: Context) {

        val conversations = ArrayList<Conversation>()
        val cursor = context.contentResolver.query(CONVERSATIONS_CONTENT_PROVIDER, ALL_THREADS_PROJECTION, null, null, "date desc")

        while (cursor.moveToNext()) conversations.add(Conversation(
                cursor.getLong(ID),
                cursor.getLong(DATE),
                cursor.getInt(MESSAGE_COUNT),
                cursor.getInt(RECIPIENT_IDS),
                cursor.getString(SNIPPET) ?: "",
                cursor.getString(SNIPPET_CS) ?: "",
                cursor.getInt(READ),
                cursor.getInt(ERROR),
                cursor.getInt(HAS_ATTACHMENT)))

        cursor.close()

        Realm.getDefaultInstance().executeTransaction {
            it.insert(conversations)
        }
    }

}
