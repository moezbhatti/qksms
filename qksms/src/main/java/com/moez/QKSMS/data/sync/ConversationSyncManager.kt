package com.moez.QKSMS.data.sync

import android.net.Uri
import android.util.Log
import com.moez.QKSMS.model.Conversation
import io.realm.Realm

internal object ConversationSyncManager {
    val TAG = "SyncManager"

    val ID = 0
    val DATE = 1
    val MESSAGE_COUNT = 2
    val RECIPIENT_IDS = 3
    val SNIPPET = 4
    val SNIPPET_CS = 5
    val READ = 6
    val ERROR = 7
    val HAS_ATTACHMENT = 8

    val CONVERSATIONS_CONTENT_PROVIDER = Uri.parse("content://mms-sms/conversations?simple=true")
    val ALL_THREADS_PROJECTION = arrayOf(
            android.provider.Telephony.Threads._ID,
            android.provider.Telephony.Threads.DATE,
            android.provider.Telephony.Threads.MESSAGE_COUNT,
            android.provider.Telephony.Threads.RECIPIENT_IDS,
            android.provider.Telephony.Threads.SNIPPET,
            android.provider.Telephony.Threads.SNIPPET_CHARSET,
            android.provider.Telephony.Threads.READ,
            android.provider.Telephony.Threads.ERROR,
            android.provider.Telephony.Threads.HAS_ATTACHMENT)

    fun copyToRealm(context: android.content.Context) {

        val realm = Realm.getDefaultInstance()
        val cursor = context.contentResolver.query(ConversationSyncManager.CONVERSATIONS_CONTENT_PROVIDER, ConversationSyncManager.ALL_THREADS_PROJECTION, null, null, "date desc")

        realm.executeTransaction {
            it.delete(Conversation::class.java)

            while (cursor.moveToNext())
                it.insertOrUpdate(Conversation(
                        cursor.getLong(ID),
                        cursor.getLong(DATE),
                        cursor.getInt(MESSAGE_COUNT),
                        cursor.getInt(RECIPIENT_IDS),
                        cursor.getString(SNIPPET) ?: "",
                        cursor.getString(SNIPPET_CS) ?: "",
                        cursor.getInt(READ),
                        cursor.getInt(ERROR),
                        cursor.getInt(HAS_ATTACHMENT)))
        }

        Log.i(TAG, "Inserted ${cursor.count} conversations")
        cursor.close()
    }

}
