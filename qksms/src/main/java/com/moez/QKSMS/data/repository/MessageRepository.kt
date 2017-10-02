package com.moez.QKSMS.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.moez.QKSMS.common.util.extensions.insertOrUpdate
import com.moez.QKSMS.data.datasource.MessageTransaction
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.sync.MessageColumns
import com.moez.QKSMS.data.sync.SyncManager
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
        realmMessageTransaction.markSeen()
    }

    fun markSeen(threadId: Long) {
        realmMessageTransaction.markSeen(threadId)
    }

    fun markRead(threadId: Long) {
        nativeMessageTransaction.markRead(threadId)
        realmMessageTransaction.markRead(threadId)
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