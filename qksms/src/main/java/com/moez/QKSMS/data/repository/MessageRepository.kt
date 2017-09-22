package com.moez.QKSMS.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.sync.MessageColumns
import com.moez.QKSMS.data.sync.SyncManager
import io.realm.Realm
import io.realm.RealmResults

class MessageRepository(val context: Context) {

    fun getConversationsAsync(): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .findAllAsync()
    }

    fun getConversationAsync(threadId: Long): Conversation {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirstAsync()
    }

    fun getUnreadUnseenMessages(): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("seen", false)
                .equalTo("read", false)
                .findAllSorted("date")
    }

    fun insertMessage(address: String, body: String, time: Long) {
        val cv = ContentValues()

        cv.put("address", address)
        cv.put("body", body)
        cv.put("date_sent", time)

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(Uri.parse("content://sms/inbox"), cv)
        copyLatestMessageToRealm(uri, contentResolver)
    }

    fun sendMessage(threadId: Long, address: String, body: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(address, null, body, null, null)

        val values = ContentValues()
        values.put("address", address)
        values.put("body", body)
        values.put("date", System.currentTimeMillis())
        values.put("read", 1)
        values.put("type", 4)
        values.put("thread_id", threadId)

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(Uri.parse("content://sms/"), values)
        copyLatestMessageToRealm(uri, contentResolver)
    }

    // TODO this is really sloppy, it should be fixed
    private fun copyLatestMessageToRealm(uri: Uri, contentResolver: ContentResolver) {
        val projection = arrayOf(Telephony.Sms.Conversations.THREAD_ID)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        if (cursor.moveToFirst()) {
            val threadId = cursor.getLong(0)

            val threadUri = Uri.withAppendedPath(MessageColumns.URI, threadId.toString())
            val messageCursor = contentResolver.query(threadUri, MessageColumns.PROJECTION, null, null, "date DESC")

            if (messageCursor.moveToFirst()) {
                val columns = MessageColumns(messageCursor)
                val message = SyncManager.messageFromCursor(threadId, messageCursor, columns)
                val realm = Realm.getDefaultInstance()
                realm.executeTransaction { it.insertOrUpdate(message) }
                realm.close()
            }
            messageCursor.close()
        }
        cursor.close()
    }

}