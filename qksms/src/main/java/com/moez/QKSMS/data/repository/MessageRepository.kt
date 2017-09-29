package com.moez.QKSMS.data.repository

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.BaseColumns
import android.provider.Telephony.Sms
import android.telephony.SmsManager
import com.moez.QKSMS.common.util.NotificationManager
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.common.util.extensions.insertOrUpdate
import com.moez.QKSMS.data.datasource.native.NativeMessageTransaction
import com.moez.QKSMS.data.datasource.realm.RealmMessageTransaction
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
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
        private val context: Context,
        private val notificationManager: NotificationManager,
        private val realmMessageTransaction: RealmMessageTransaction) {

    // TODO figure out why injecting this in the constructor breaks Dagger
    private val nativeMessageTransaction: NativeMessageTransaction = NativeMessageTransaction(context)

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

    fun insertReceivedMessage(address: String, body: String, time: Long) {
        val values = ContentValues()
        values.put(Sms.ADDRESS, address)
        values.put(Sms.BODY, body)
        values.put(Sms.DATE_SENT, time)

        val contentResolver = context.contentResolver
        Flowable.just(values)
                .subscribeOn(Schedulers.io())
                .map { contentResolver.insert(Sms.Inbox.CONTENT_URI, values) }
                .subscribe { uri ->
                    copyMessageToRealm(uri)
                    notificationManager.update(this)
                }
    }

    fun markAllSeen() {
        // Messages in SMS ContentProvider
        val projection = arrayOf(BaseColumns._ID)
        val selection = "${Sms.SEEN} = 0"
        val contentResolver = context.contentResolver
        contentResolver.query(Sms.Inbox.CONTENT_URI, projection, selection, null, null)
                .asFlowable()
                .subscribeOn(Schedulers.io())
                .map { cursor -> cursor.getLong(0) }
                .map { id -> Uri.withAppendedPath(Sms.CONTENT_URI, id.toString()) }
                .subscribe { uri ->
                    val values = ContentValues()
                    values.put(Sms.SEEN, true)
                    contentResolver.update(uri, values, null, null)
                }

        // TODO also need to mark MMS in ContentProvider as Seen

        // Messages in Realm
        Schedulers.io().scheduleDirect {
            val realm = Realm.getDefaultInstance()
            val messages = realm.where(Message::class.java).equalTo("seen", false).findAll()
            realm.executeTransaction { messages.forEach { message -> message.seen = true } }
            realm.close()
        }
    }

    fun markSeen(threadId: Long) {
        // Messages in SMS ContentProvider
        val projection = arrayOf(BaseColumns._ID)
        val selection = "${Sms.THREAD_ID} = $threadId AND ${Sms.SEEN} = 0"
        val contentResolver = context.contentResolver
        contentResolver.query(Sms.Inbox.CONTENT_URI, projection, selection, null, null)
                .asFlowable()
                .subscribeOn(Schedulers.io())
                .map { cursor -> cursor.getLong(0) }
                .map { id -> Uri.withAppendedPath(Sms.CONTENT_URI, id.toString()) }
                .subscribe { uri ->
                    val values = ContentValues()
                    values.put(Sms.SEEN, true)
                    contentResolver.update(uri, values, null, null)
                }

        // TODO also need to mark MMS in ContentProvider as Seen

        // Messages in Realm
        Schedulers.io().scheduleDirect {
            val realm = Realm.getDefaultInstance()
            val messages = realm.where(Message::class.java)
                    .equalTo("threadId", threadId)
                    .equalTo("seen", false)
                    .findAll()

            realm.executeTransaction {
                messages.forEach { message ->
                    message.seen = true
                }
            }
            realm.close()
        }
    }

    fun markRead(threadId: Long) {
        nativeMessageTransaction.markRead(threadId)
        realmMessageTransaction.markRead(threadId)
    }

    fun markSent(uri: Uri) {
        val values = ContentValues()
        values.put(Sms.TYPE, Sms.MESSAGE_TYPE_SENT)
        updateMessage(values, uri)
    }

    fun markFailed(uri: Uri, resultCode: Int) {
        val values = ContentValues()
        values.put(Sms.TYPE, Sms.MESSAGE_TYPE_FAILED)
        values.put(Sms.ERROR_CODE, resultCode)
        updateMessage(values, uri)
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
                    copyMessageToRealm(uri)

                    val sentIntent = Intent(context, MessageSentReceiver::class.java).putExtra("uri", uri.toString())
                    val sentPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), sentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val deliveredIntent = Intent(context, MessageDeliveredReceiver::class.java).putExtra("uri", uri.toString())
                    val deliveredPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(address, null, body, sentPI, deliveredPI)
                }
    }

    private fun updateMessage(values: ContentValues, uri: Uri) {
        val contentResolver = context.contentResolver
        Flowable.just(values)
                .subscribeOn(Schedulers.io())
                .map { contentResolver.update(uri, values, null, null) }
                .subscribe { copyMessageToRealm(uri) }
    }

    private fun copyMessageToRealm(uri: Uri) {
        val cursor = context.contentResolver.query(uri, null, null, null, "date DESC")
        if (cursor.moveToFirst()) {
            val columns = MessageColumns(cursor)
            val message = SyncManager.messageFromCursor(cursor, columns)
            message.insertOrUpdate()
        }
        cursor.close()
    }

}