/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.Telephony
import android.provider.Telephony.Sms
import android.provider.Telephony.TextBasedSmsColumns
import common.util.Keys
import common.util.MessageUtils
import common.util.extensions.asFlowable
import common.util.extensions.asMaybe
import common.util.extensions.insertOrUpdate
import common.util.extensions.mapNotNull
import data.mapper.CursorToConversation
import data.mapper.CursorToRecipient
import data.model.Conversation
import data.model.InboxItem
import data.model.Message
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
        private val context: Context,
        private val messageIds: Keys,
        private val cursorToConversation: CursorToConversation,
        private val cursorToRecipient: CursorToRecipient) {

    fun getConversations(archived: Boolean = false): Flowable<List<InboxItem>> {
        val realm = Realm.getDefaultInstance()

        val conversationFlowable = realm.where(Conversation::class.java)
                .equalTo("archived", archived)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }

        val messageFlowable = realm.where(Message::class.java)
                .sort("date", Sort.DESCENDING)
                .distinctValues("threadId")
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }

        return Flowables.combineLatest(conversationFlowable, messageFlowable, { conversations, messages ->
            val conversationMap = conversations.associateBy { conversation -> conversation.id }

            messages.mapNotNull { message ->
                val conversation = conversationMap[message.threadId]
                if (conversation == null) null else InboxItem(conversation, message)
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

    fun getOrCreateConversation(address: String): Maybe<Conversation> {
        return getOrCreateConversation(listOf(address))
    }

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
                    var conversation = getConversation(threadId)
                    if (conversation != null) conversation = Realm.getDefaultInstance().copyFromRealm(conversation)

                    conversation ?: getConversationFromCp(threadId)?.apply { insertOrUpdate() } ?: Conversation()
                }
                .onErrorReturn { Conversation() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .findAllSorted("date")
    }

    /**
     * Retrieves the list of messages which should be shown in the notification
     * for a given conversation
     */
    fun getUnreadUnseenMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("seen", false)
                .equalTo("read", false)
                .equalTo("threadId", threadId)
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

    fun insertSentSms(threadId: Long, address: String, body: String): Message {

        // Insert the message to Realm
        val message = Message().apply {
            this.threadId = threadId
            this.address = address
            this.body = body
            this.date = System.currentTimeMillis()

            id = messageIds.newId()
            boxId = Sms.MESSAGE_TYPE_OUTBOX
            type = "sms"
            read = true
            seen = true
        }
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { realm.insert(message) }
        realm.close()

        // Insert the message to the native content provider
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, System.currentTimeMillis())
            put(Telephony.Sms.READ, true)
            put(Telephony.Sms.SEEN, true)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
            put(Telephony.Sms.THREAD_ID, threadId)
        }
        context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)

        return message
    }

    fun insertReceivedSms(address: String, body: String, sentTime: Long): Message {

        // Insert the message to Realm
        val realm = Realm.getDefaultInstance()
        val message = Message().apply {
            this.address = address
            this.body = body
            this.dateSent = sentTime
            this.date = System.currentTimeMillis()

            id = messageIds.newId()
            threadId = getOrCreateConversation(address).blockingGet().id
            boxId = Sms.MESSAGE_TYPE_INBOX
            type = "sms"

            realm.executeTransaction { realm.insert(this) }
        }
        realm.close()

        // Insert the message to the native content provider
        ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE_SENT, sentTime)
            put(Telephony.Sms.READ, false)
            put(Telephony.Sms.SEEN, false)

            context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, this)
        }

        return message
    }

    fun markSent(id: Long): Flowable<*> {
        val realm = Realm.getDefaultInstance()
        return realm.where(Message::class.java).equalTo("id", id)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
                .filter { it.isValid }
                .mapNotNull { it[0] }
                .take(1)
                .doOnNext { message ->
                    // Update the message in realm
                    realm.executeTransaction {
                        message.boxId = Sms.MESSAGE_TYPE_SENT
                    }
                }
                .map { message -> message.getUri() }
                .observeOn(Schedulers.computation())
                .doOnNext { uri ->
                    // Update the message in the native ContentProvider
                    val values = ContentValues()
                    values.put(Sms.TYPE, Sms.MESSAGE_TYPE_SENT)
                    context.contentResolver.update(uri, values, null, null)
                }
    }

    fun markFailed(id: Long, resultCode: Int): Flowable<*> {
        val realm = Realm.getDefaultInstance()
        return realm.where(Message::class.java).equalTo("id", id)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
                .filter { it.isValid }
                .mapNotNull { it[0] }
                .take(1)
                .doOnNext { message ->
                    // Update the message in realm
                    realm.executeTransaction {
                        message.boxId = Sms.MESSAGE_TYPE_FAILED
                        message.errorCode = resultCode
                    }
                }
                .map { message -> message.getUri() }
                .observeOn(Schedulers.computation())
                .doOnNext { uri ->
                    // Update the message in the native ContentProvider
                    val values = ContentValues()
                    values.put(Sms.TYPE, Sms.MESSAGE_TYPE_FAILED)
                    values.put(Sms.ERROR_CODE, resultCode)
                    context.contentResolver.update(uri, values, null, null)
                }
    }

    fun markDelivered(id: Long): Flowable<*> {
        val realm = Realm.getDefaultInstance()
        return realm.where(Message::class.java).equalTo("id", id)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
                .filter { it.isValid }
                .mapNotNull { it[0] }
                .take(1)
                .doOnNext { message ->
                    // Update the message in realm
                    realm.executeTransaction {
                        message.deliveryStatus = Message.DeliveryStatus.RECEIVED
                        message.dateSent = System.currentTimeMillis()
                        message.read = true
                    }
                }
                .map { message -> message.getUri() }
                .observeOn(Schedulers.computation())
                .doOnNext { uri ->
                    // Update the message in the native ContentProvider
                    val values = ContentValues()
                    values.put(Sms.STATUS, TextBasedSmsColumns.STATUS_COMPLETE)
                    values.put(Sms.DATE_SENT, System.currentTimeMillis())
                    values.put(Sms.READ, true)
                    context.contentResolver.update(uri, values, null, null)
                }
    }

    fun markDeliveryFailed(id: Long, resultCode: Int): Flowable<*> {
        val realm = Realm.getDefaultInstance()
        return realm.where(Message::class.java).equalTo("id", id)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
                .filter { it.isValid }
                .mapNotNull { it[0] }
                .take(1)
                .doOnNext { message ->
                    // Update the message in realm
                    realm.executeTransaction {
                        message.deliveryStatus = Message.DeliveryStatus.FAILED
                        message.dateSent = System.currentTimeMillis()
                        message.read = true
                        message.errorCode = resultCode
                    }
                }
                .map { message -> message.getUri() }
                .observeOn(Schedulers.computation())
                .doOnNext { uri ->
                    // Update the message in the native ContentProvider
                    val values = ContentValues()
                    values.put(Sms.STATUS, TextBasedSmsColumns.STATUS_FAILED)
                    values.put(Sms.DATE_SENT, System.currentTimeMillis())
                    values.put(Sms.READ, true)
                    values.put(Sms.ERROR_CODE, resultCode)
                    context.contentResolver.update(uri, values, null, null)
                }
    }

    fun deleteMessage(messageId: Long) {
        val realm = Realm.getDefaultInstance()
        realm.where(Message::class.java).equalTo("id", messageId).findFirst()?.let { message ->
            val uri = message.getUri()
            realm.executeTransaction { message.deleteFromRealm() }
            context.contentResolver.delete(uri, null, null)
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

    private fun getConversationFromCp(threadId: Long): Conversation? {
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

            conversation.recipients.toFlowable()
                    .map { recipient -> recipient.id.toString() }
                    .map { id -> context.contentResolver.query(CursorToRecipient.URI, null, "_id = ?", arrayOf(id), null) }
                    .flatMap { recipientCursor -> recipientCursor.asFlowable().map { cursor -> cursorToRecipient.map(cursor) } }
                    .doOnNext { recipient -> recipient.insertOrUpdate() }
                    .blockingSubscribe()
        }

        cursor.close()

        return conversation
    }

}