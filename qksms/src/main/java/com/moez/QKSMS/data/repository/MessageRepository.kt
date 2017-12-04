package com.moez.QKSMS.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.Telephony
import android.provider.Telephony.Sms
import android.provider.Telephony.TextBasedSmsColumns
import com.moez.QKSMS.common.util.MessageUtils
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.common.util.extensions.asMaybe
import com.moez.QKSMS.common.util.extensions.insertOrUpdate
import com.moez.QKSMS.data.mapper.CursorToConversation
import com.moez.QKSMS.data.mapper.CursorToMessage
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.ConversationMessagePair
import com.moez.QKSMS.data.model.Message
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
        private val context: Context,
        private val cursorToMessage: CursorToMessage,
        private val cursorToConversation: CursorToConversation) {

    fun getConversations(archived: Boolean = false): Flowable<List<ConversationMessagePair>> {
        val realm = Realm.getDefaultInstance()

        val conversations = realm.where(Conversation::class.java)
                .equalTo("archived", archived)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
                .map { conversations -> conversations.associateBy { conversation -> conversation.id } }

        val messages = realm.where(Message::class.java)
                .findAllSortedAsync("date", Sort.DESCENDING)
                .where()
                .distinctAsync("threadId")
                .asFlowable()
                .filter { it.isLoaded }

        return Flowable.combineLatest(conversations, messages, BiFunction { conversations, messages ->
            messages.mapNotNull { message ->
                val conversation = conversations[message.threadId]
                if (conversation == null) null else ConversationMessagePair(conversation, message)
            }
        })
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

    /**
     * Gets an unmanaged conversation by the address
     */
    fun getOrCreateConversation(address: String) = getOrCreateConversation(listOf(address))

    fun getOrCreateConversation(addresses: List<String>): Maybe<Conversation> {
        return Maybe.just(addresses)
                .map { recipients ->
                    recipients.map { address ->
                        when (MessageUtils.isEmailAddress(address)) {
                            true -> MessageUtils.extractAddrSpec(address)
                            false -> address
                        }
                    }
                }
                .map { recipients ->
                    Uri.parse("content://mms-sms/threadID").buildUpon().apply {
                        recipients.forEach { recipient -> appendQueryParameter("recipient", recipient) }
                    }
                }
                .flatMap { uriBuilder ->
                    context.contentResolver.query(uriBuilder.build(), arrayOf(BaseColumns._ID), null, null, null).asMaybe()
                }
                .map { cursor -> cursor.getLong(0) }
                .filter { threadId -> threadId != 0L }
                .map { threadId ->
                    getConversation(threadId)
                            ?: getConversationFromCp(threadId)?.apply { insertOrUpdate() }
                            ?: Conversation()
                }
                .onErrorReturn { Conversation() }
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

    fun markArchived(threadId: Long) {
        val realm = Realm.getDefaultInstance()
        val conversation = realm.where(Conversation::class.java)
                .equalTo("id", threadId)
                .equalTo("archived", false)
                .findFirst()

        realm.executeTransaction { conversation?.archived = true }
        realm.close()
    }

    fun markUnarchived(threadId: Long) {
        val realm = Realm.getDefaultInstance()
        val conversation = realm.where(Conversation::class.java)
                .equalTo("id", threadId)
                .equalTo("archived", true)
                .findFirst()

        realm.executeTransaction { conversation?.archived = false }
        realm.close()
    }

    fun markAllSeen() {
        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Message::class.java).equalTo("seen", false).findAll()
        realm.executeTransaction { messages.forEach { message -> message.seen = true } }
        realm.close()
    }

    fun markSeen(threadId: Long) {
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

    fun markRead(threadId: Long) {
        // TODO also need to mark MMS in ContentProvider as Read
        val projection = arrayOf(BaseColumns._ID)
        val selection = "${Sms.THREAD_ID} = $threadId AND (${Sms.SEEN} = 0 OR ${Sms.READ} = 0)"
        val contentResolver = context.contentResolver
        contentResolver.query(Sms.Inbox.CONTENT_URI, projection, selection, null, null)
                .asFlowable()
                .subscribeOn(Schedulers.io())
                .map { cursor -> cursor.getLong(0) }
                .map { id -> Uri.withAppendedPath(Sms.CONTENT_URI, id.toString()) }
                .subscribe { uri ->
                    val values = ContentValues()
                    values.put(Sms.SEEN, true)
                    values.put(Sms.READ, true)
                    contentResolver.update(uri, values, null, null)
                }


        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Message::class.java)
                .equalTo("threadId", threadId)
                .beginGroup()
                .equalTo("read", false)
                .or()
                .equalTo("seen", false)
                .endGroup()
                .findAll()

        realm.executeTransaction {
            messages.forEach { message ->
                message.seen = true
                message.read = true
            }
        }
        realm.close()
    }

    fun markSent(uri: Uri) {
        val values = ContentValues()
        values.put(Sms.TYPE, Sms.MESSAGE_TYPE_SENT)
        updateMessageFromUri(uri, values)
    }

    fun markFailed(uri: Uri, resultCode: Int) {
        val values = ContentValues()
        values.put(Sms.TYPE, Sms.MESSAGE_TYPE_FAILED)
        values.put(Sms.ERROR_CODE, resultCode)
        updateMessageFromUri(uri, values)
    }

    fun markDelivered(uri: Uri) {
        val values = ContentValues()
        values.put("status", TextBasedSmsColumns.STATUS_COMPLETE)
        values.put("date_sent", Calendar.getInstance().timeInMillis)
        values.put("read", true)
        updateMessageFromUri(uri, values)
    }

    fun markDeliveryFailed(uri: Uri, resultCode: Int) {
        val values = ContentValues()
        values.put("status", TextBasedSmsColumns.STATUS_FAILED)
        values.put("date_sent", Calendar.getInstance().timeInMillis)
        values.put("read", true)
        values.put("error_code", resultCode)
        updateMessageFromUri(uri, values)
    }

    fun deleteMessage(messageId: Long) {
        val realm = Realm.getDefaultInstance()
        realm.where(Message::class.java).equalTo("id", messageId).findFirst()?.let { message ->
            context.contentResolver.delete(message.getUri(), null, null)
            realm.executeTransaction { message.deleteFromRealm() }
        }
        realm.close()
    }

    fun deleteConversation(threadId: Long) {
        val realm = Realm.getDefaultInstance()

        val conversation = realm.where(Conversation::class.java).equalTo("id", threadId).findAll()
        val messages = realm.where(Message::class.java).equalTo("threadId", threadId).findAll()

        realm.executeTransaction {
            conversation.deleteAllFromRealm()
            messages.deleteAllFromRealm()
        }

        realm.close()

        val uri = Uri.withAppendedPath(Telephony.Threads.CONTENT_URI, threadId.toString())
        context.contentResolver.delete(uri, null, null)
    }

    fun getConversationFromCp(threadId: Long): Conversation? {
        var conversation: Conversation? = null

        val cursor = context.contentResolver.query(
                CursorToConversation.URI,
                CursorToConversation.PROJECTION,
                "_id = ?",
                arrayOf(threadId.toString()),
                null)

        if (cursor.moveToFirst()) {
            conversation = cursorToConversation.map(cursor)
            conversation.insertOrUpdate()
        }

        cursor.close()

        return conversation
    }

    fun updateMessageFromUri(uri: Uri, values: ContentValues) {
        val contentResolver = context.contentResolver
        Flowable.just(values)
                .subscribeOn(Schedulers.io())
                .map { contentResolver.update(uri, values, null, null) }
                .subscribe { addMessageFromUri(uri) }
    }

    fun addMessageFromUri(uri: Uri) {
        val cursor = context.contentResolver.query(uri, null, null, null, "date DESC")
        val columnsMap = CursorToMessage.MessageColumns(cursor)

        if (cursor.moveToFirst()) {
            cursorToMessage.map(Pair(cursor, columnsMap)).insertOrUpdate()
        }

        cursor.close()
    }

}