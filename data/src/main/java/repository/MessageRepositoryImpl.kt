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
package repository

import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.BaseColumns
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import com.klinker.android.send_message.BroadcastUtils
import com.klinker.android.send_message.StripAccents
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import manager.KeyManager
import mapper.CursorToConversation
import mapper.CursorToRecipient
import model.Contact
import model.Conversation
import model.Message
import model.MmsPart
import util.MessageUtils
import util.Preferences
import util.extensions.asFlowable
import util.extensions.asMaybe
import util.extensions.asObservable
import util.extensions.insertOrUpdate
import util.extensions.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
        private val context: Context,
        private val messageIds: KeyManager,
        private val cursorToConversation: CursorToConversation,
        private val cursorToRecipient: CursorToRecipient,
        private val prefs: Preferences) : MessageRepository {

    override fun getConversations(archived: Boolean): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .distinctValues("threadId")
                .equalTo("conversation.archived", archived)
                .equalTo("conversation.blocked", false)
                .sort("date", Sort.DESCENDING)
                .findAllAsync()
    }

    override fun getConversationsForWidget(): List<Message> {
        val realm = Realm.getDefaultInstance()
        return realm.copyFromRealm(realm.where(Message::class.java)
                .distinctValues("threadId")
                .equalTo("conversation.archived", false)
                .equalTo("conversation.blocked", false)
                .sort("date", Sort.DESCENDING)
                .findAllAsync())
    }

    override fun getBlockedConversations(): Flowable<List<Conversation>> {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("blocked", true)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
                .map { it.toList() }
    }

    override fun getConversationAsync(threadId: Long): Conversation {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirstAsync()
    }

    override fun getConversation(threadId: Long): Conversation? {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()
    }

    override fun getOrCreateConversation(threadId: Long): Conversation? {
        return getConversation(threadId) ?: getConversationFromCp(threadId)
    }

    override fun getOrCreateConversation(address: String): Maybe<Conversation> {
        return getOrCreateConversation(listOf(address))
    }

    override fun getOrCreateConversation(addresses: List<String>): Maybe<Conversation> {
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

    override fun saveDraft(threadId: Long, draft: String) {
        Realm.getDefaultInstance().use { realm ->
            val conversation = realm.where(Conversation::class.java)
                    .equalTo("id", threadId)
                    .findFirst()

            conversation?.let {
                realm.executeTransaction {
                    conversation.draft = draft
                }
            }
        }
    }

    override fun getMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .sort("date")
                .findAllAsync()
    }

    override fun getMessage(id: Long): Message? {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    override fun getMessageForPart(id: Long): Message? {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("parts.id", id)
                .findFirst()
    }

    override fun getUnreadMessageCount(): Long {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("read", false)
                .count()
    }

    override fun getPart(id: Long): MmsPart? {
        return Realm.getDefaultInstance()
                .where(MmsPart::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    override fun getPartsForConversation(threadId: Long): Observable<List<MmsPart>> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .isNotEmpty("parts")
                .sort("date")
                .findAll()
                .asObservable()
                .map { messages -> messages.flatMap { it.parts } }
    }

    /**
     * Retrieves the list of messages which should be shown in the notification
     * for a given conversation
     */
    override fun getUnreadUnseenMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("seen", false)
                .equalTo("read", false)
                .equalTo("threadId", threadId)
                .sort("date")
                .findAll()
    }

    override fun markArchived(threadId: Long) {
        val realm = Realm.getDefaultInstance()
        val conversation = realm.where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()

        realm.executeTransaction { conversation?.archived = true }
        realm.close()
    }

    override fun markUnarchived(threadId: Long) {
        val realm = Realm.getDefaultInstance()
        val conversation = realm.where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()

        realm.executeTransaction { conversation?.archived = false }
        realm.close()
    }

    override fun markBlocked(threadId: Long) {
        val realm = Realm.getDefaultInstance()
        val conversation = realm.where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()

        realm.executeTransaction { conversation?.blocked = true }
        realm.close()
    }

    override fun markUnblocked(threadId: Long) {
        val realm = Realm.getDefaultInstance()
        val conversation = realm.where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()

        realm.executeTransaction { conversation?.blocked = false }
        realm.close()
    }

    override fun markAllSeen() {
        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Message::class.java).equalTo("seen", false).findAll()
        realm.executeTransaction { messages.forEach { message -> message.seen = true } }
        realm.close()
    }

    override fun markSeen(threadId: Long) {
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

    override fun markRead(threadId: Long) {
        // TODO also need to mark MMS in ContentProvider as Read
        val projection = arrayOf(BaseColumns._ID)
        val selection = "${Telephony.Sms.THREAD_ID} = $threadId AND (${Telephony.Sms.SEEN} = 0 OR ${Telephony.Sms.READ} = 0)"
        val contentResolver = context.contentResolver
        contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, projection, selection, null, null)
                .asFlowable()
                .subscribeOn(Schedulers.io())
                .map { cursor -> cursor.getLong(0) }
                .map { id -> Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, id.toString()) }
                .subscribe { uri ->
                    val values = ContentValues()
                    values.put(Telephony.Sms.SEEN, true)
                    values.put(Telephony.Sms.READ, true)
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

    override fun sendSmsAndPersist(threadId: Long, address: String, body: String) {
        val message = insertSentSms(threadId, address, body)
        sendSms(message)
    }

    override fun sendSms(message: Message) {
        val smsManager = SmsManager.getDefault()

        val parts = smsManager.divideMessage(if (prefs.unicode.get()) StripAccents.stripAccents(message.body) else message.body)

        val sentIntents = parts.map {
            val action = "com.moez.QKSMS.SMS_SENT"
            val intent = Intent(action).putExtra("id", message.id)
            BroadcastUtils.addClassName(context, intent, action)
            PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val deliveredIntents = parts.map {
            val action = "com.moez.QKSMS.SMS_DELIVERED"
            val intent = Intent(action).putExtra("id", message.id)
            BroadcastUtils.addClassName(context, intent, action)
            val pendingIntent = PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
            if (prefs.delivery.get()) pendingIntent else null
        }

        smsManager.sendMultipartTextMessage(message.address, null, parts, ArrayList(sentIntents), ArrayList(deliveredIntents))
    }

    override fun insertSentSms(threadId: Long, address: String, body: String): Message {

        // Insert the message to Realm
        val message = Message().apply {
            this.address = address
            this.body = body
            this.date = System.currentTimeMillis()

            id = messageIds.newId()
            boxId = Telephony.Sms.MESSAGE_TYPE_OUTBOX
            type = "sms"
            read = true
            seen = true

            conversation = getOrCreateConversation(threadId)
        }
        val realm = Realm.getDefaultInstance()
        var managedMessage: Message? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

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
        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)

        // Update the contentId after the message has been inserted to the content provider
        realm.executeTransaction { managedMessage?.contentId = uri.lastPathSegment.toLong() }
        realm.close()

        return message
    }

    override fun insertReceivedSms(address: String, body: String, sentTime: Long): Message {

        // Insert the message to Realm
        val message = Message().apply {
            this.address = address
            this.body = body
            this.dateSent = sentTime
            this.date = System.currentTimeMillis()

            id = messageIds.newId()
            boxId = Telephony.Sms.MESSAGE_TYPE_INBOX
            type = "sms"

            conversation = getOrCreateConversation(address).blockingGet()
        }
        val realm = Realm.getDefaultInstance()
        var managedMessage: Message? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

        // Insert the message to the native content provider
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE_SENT, sentTime)
        }
        val uri = context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)

        // Update the contentId after the message has been inserted to the content provider
        realm.executeTransaction { managedMessage?.contentId = uri.lastPathSegment.toLong() }
        realm.close()

        return message
    }

    /**
     * Marks the message as sending, in case we need to retry sending it
     */
    override fun markSending(id: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.boxId = Telephony.Sms.MESSAGE_TYPE_OUTBOX
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markSent(id: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.boxId = Telephony.Sms.MESSAGE_TYPE_SENT
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markFailed(id: Long, resultCode: Int) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.boxId = Telephony.Sms.MESSAGE_TYPE_FAILED
                    message.errorCode = resultCode
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_FAILED)
                values.put(Telephony.Sms.ERROR_CODE, resultCode)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markDelivered(id: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.deliveryStatus = Telephony.Sms.STATUS_COMPLETE
                    message.dateSent = System.currentTimeMillis()
                    message.read = true
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_COMPLETE)
                values.put(Telephony.Sms.DATE_SENT, System.currentTimeMillis())
                values.put(Telephony.Sms.READ, true)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markDeliveryFailed(id: Long, resultCode: Int) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.deliveryStatus = Telephony.Sms.STATUS_FAILED
                    message.dateSent = System.currentTimeMillis()
                    message.read = true
                    message.errorCode = resultCode
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_FAILED)
                values.put(Telephony.Sms.DATE_SENT, System.currentTimeMillis())
                values.put(Telephony.Sms.READ, true)
                values.put(Telephony.Sms.ERROR_CODE, resultCode)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun deleteMessage(messageId: Long) {
        val realm = Realm.getDefaultInstance()
        realm.where(Message::class.java).equalTo("id", messageId).findFirst()?.let { message ->
            val uri = message.getUri()
            realm.executeTransaction { message.deleteFromRealm() }
            context.contentResolver.delete(uri, null, null)
        }
        realm.close()
    }

    override fun deleteConversation(threadId: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversation = realm.where(Conversation::class.java).equalTo("id", threadId).findAll()
            val messages = realm.where(Message::class.java).equalTo("threadId", threadId).findAll()

            realm.executeTransaction {
                conversation.deleteAllFromRealm()
                messages.deleteAllFromRealm()
            }
        }

        val uri = ContentUris.withAppendedId(Telephony.Threads.CONTENT_URI, threadId)
        context.contentResolver.delete(uri, null, null)
    }

    private fun getConversationFromCp(threadId: Long): Conversation? {
        var conversation: Conversation? = null

        val cursor = cursorToConversation.getConversationCursor(threadId)

        if (cursor.moveToFirst()) {
            conversation = cursorToConversation.map(cursor)

            val realm = Realm.getDefaultInstance()
            val contacts = realm.copyFromRealm(realm.where(Contact::class.java).findAll())

            val recipients = conversation.recipients
                    .map { recipient -> recipient.id }
                    .map { id -> cursorToRecipient.getRecipientCursor(id) }
                    .map { recipientCursor -> recipientCursor.map { cursorToRecipient.map(recipientCursor) } }
                    .flatten()
                    .map { recipient ->
                        recipient.apply {
                            contact = contacts.firstOrNull {
                                it.numbers.any { PhoneNumberUtils.compare(recipient.address, it.address) }
                            }
                        }
                    }

            conversation.recipients.clear()
            conversation.recipients.addAll(recipients)
            conversation.insertOrUpdate()
            realm.close()
        }

        cursor.close()

        return conversation
    }

}