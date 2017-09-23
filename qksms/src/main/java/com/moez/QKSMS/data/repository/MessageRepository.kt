package com.moez.QKSMS.data.repository

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.sync.MessageColumns
import com.moez.QKSMS.data.sync.SyncManager
import com.moez.QKSMS.receiver.MessageDeliveredReceiver
import com.moez.QKSMS.receiver.MessageSentReceiver
import io.realm.Realm
import io.realm.RealmResults

class MessageRepository(val context: Context) {

    fun getConversationsAsync(): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .isNotEmpty("messages")
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
        val values = ContentValues()
        values.put("address", address)
        values.put("body", body)
        values.put("date", System.currentTimeMillis())
        values.put("read", true)
        values.put("type", Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX)
        values.put("thread_id", threadId)

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(Uri.parse("content://sms/"), values)
        copyLatestMessageToRealm(uri, contentResolver)

        val sentIntent = Intent(context, MessageSentReceiver::class.java).putExtra("uri", uri.toString())
        val sentPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), sentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val deliveredIntent = Intent(context, MessageDeliveredReceiver::class.java).putExtra("uri", uri.toString())
        val deliveredPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(address, null, body, sentPI, deliveredPI)
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