package com.moez.QKSMS.data.repository

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony.Sms
import android.telephony.SmsManager
import com.moez.QKSMS.common.util.extensions.insertOrUpdate
import com.moez.QKSMS.data.datasource.MessageTransaction
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.sync.MessageColumns
import com.moez.QKSMS.data.sync.SyncManager
import com.moez.QKSMS.receiver.MessageDeliveredReceiver
import com.moez.QKSMS.receiver.MessageSentReceiver
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
        private val context: Context,
        @Named("Realm") private val realmMessageTransaction: MessageTransaction,
        @Named("Native") private val nativeMessageTransaction: MessageTransaction) {

    fun getConversationMessagesAsync(): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .findAllSortedAsync("date", Sort.DESCENDING)
                .distinctAsync("threadId")
    }

    fun getConversationAsync(threadId: Long): Conversation {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirstAsync()
    }

    fun getConversation(threadId: Long): Conversation? {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()
    }

    fun getMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .findAllSorted("date")
    }

    fun getUnreadUnseenMessages(): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("seen", false)
                .equalTo("read", false)
                .findAllSorted("date")
    }

    fun markAllSeen() {
        nativeMessageTransaction.markSeen()
        realmMessageTransaction.markSeen()
    }

    fun markSeen(threadId: Long) {
        nativeMessageTransaction.markSeen(threadId)
        realmMessageTransaction.markSeen(threadId)
    }

    fun markRead(threadId: Long) {
        nativeMessageTransaction.markRead(threadId)
        realmMessageTransaction.markRead(threadId)
    }

    fun sendMessage(threadId: Long, address: String, body: String) {
        val values = ContentValues()
        values.put(Sms.ADDRESS, address)
        values.put(Sms.BODY, body)
        values.put(Sms.DATE, System.currentTimeMillis())
        values.put(Sms.READ, true)
        values.put(Sms.TYPE, Sms.MESSAGE_TYPE_OUTBOX)
        values.put(Sms.THREAD_ID, threadId)

        val contentResolver = context.contentResolver
        Flowable.just(values)
                .subscribeOn(Schedulers.io())
                .map { contentResolver.insert(Sms.CONTENT_URI, values) }
                .subscribe { uri ->
                    addMessageFromUri(uri)

                    val sentIntent = Intent(context, MessageSentReceiver::class.java).putExtra("uri", uri.toString())
                    val sentPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), sentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val deliveredIntent = Intent(context, MessageDeliveredReceiver::class.java).putExtra("uri", uri.toString())
                    val deliveredPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(address, null, body, sentPI, deliveredPI)
                }
    }

    fun updateMessageFromUri(values: ContentValues, uri: Uri) {
        val contentResolver = context.contentResolver
        Flowable.just(values)
                .subscribeOn(Schedulers.io())
                .map { contentResolver.update(uri, values, null, null) }
                .subscribe { addMessageFromUri(uri) }
    }

    fun addMessageFromUri(uri: Uri) {
        val cursor = context.contentResolver.query(uri, null, null, null, "date DESC")
        if (cursor.moveToFirst()) {
            val columns = MessageColumns(cursor)
            val message = SyncManager.messageFromCursor(cursor, columns)
            message.insertOrUpdate()
        }
        cursor.close()
    }

}