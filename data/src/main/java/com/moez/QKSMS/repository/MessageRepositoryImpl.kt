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
package com.moez.QKSMS.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.core.content.contentValuesOf
import com.google.android.mms.ContentType
import com.klinker.android.send_message.BroadcastUtils
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.StripAccents
import com.klinker.android.send_message.Transaction
import com.moez.QKSMS.compat.TelephonyCompat
import com.moez.QKSMS.extensions.anyOf
import com.moez.QKSMS.extensions.insertOrUpdate
import com.moez.QKSMS.extensions.map
import com.moez.QKSMS.manager.KeyManager
import com.moez.QKSMS.mapper.CursorToConversation
import com.moez.QKSMS.model.Attachment
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.MmsPart
import com.moez.QKSMS.util.Preferences
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
        private val context: Context,
        private val messageIds: KeyManager,
        private val imageRepository: ImageRepository,
        private val prefs: Preferences) : MessageRepository {

    override fun getMessages(threadId: Long, query: String): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .let { if (query.isEmpty()) it else it.contains("body", query, Case.INSENSITIVE) }
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

    override fun getUnreadCount(): Long {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("archived", false)
                .equalTo("blocked", false)
                .equalTo("read", false)
                .count()
    }

    override fun getPart(id: Long): MmsPart? {
        return Realm.getDefaultInstance()
                .where(MmsPart::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    override fun getPartsForConversation(threadId: Long): RealmResults<MmsPart> {
        return Realm.getDefaultInstance()
                .where(MmsPart::class.java)
                .equalTo("messages.threadId", threadId)
                .beginGroup()
                .contains("type", "image/")
                .or()
                .contains("type", "video/")
                .endGroup()
                .sort("id", Sort.DESCENDING)
                .findAllAsync()
    }

    /**
     * Retrieves the list of messages which should be shown in the notification
     * for a given conversation
     */
    override fun getUnreadUnseenMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .also { it.refresh() }
                .where(Message::class.java)
                .equalTo("seen", false)
                .equalTo("read", false)
                .equalTo("threadId", threadId)
                .sort("date")
                .findAll()
    }

    override fun getUnreadMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("read", false)
                .equalTo("threadId", threadId)
                .sort("date")
                .findAll()
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

    override fun markRead(vararg threadIds: Long) {
        Realm.getDefaultInstance()?.use { realm ->
            val messages = realm.where(Message::class.java)
                    .anyOf("threadId", threadIds)
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
        }

        val values = ContentValues()
        values.put(Telephony.Sms.SEEN, true)
        values.put(Telephony.Sms.READ, true)

        threadIds.forEach { threadId ->
            try {
                val uri = ContentUris.withAppendedId(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, threadId)
                context.contentResolver.update(uri, values, "${Telephony.Sms.READ} = 0", null)
            } catch (exception: Exception) {
                Timber.w(exception)
            }
        }
    }

    override fun markUnread(vararg threadIds: Long) {
        Realm.getDefaultInstance()?.use { realm ->
            val conversation = realm.where(Conversation::class.java)
                    .anyOf("id", threadIds)
                    .equalTo("read", true)
                    .findAll()

            realm.executeTransaction {
                conversation.forEach { it.read = false }
            }
        }
    }

    override fun sendSmsAndPersist(subId: Int, threadId: Long, address: String, body: String) {
        if (prefs.sendDelay.get() != Preferences.SEND_DELAY_NONE) {
            val delay = when (prefs.sendDelay.get()) {
                Preferences.SEND_DELAY_SHORT -> 3000
                Preferences.SEND_DELAY_MEDIUM -> 5000
                Preferences.SEND_DELAY_LONG -> 10000
                else -> 0
            }

            val sendTime = System.currentTimeMillis() + delay
            val message = insertSentSms(subId, threadId, address, body, sendTime)

            val intent = getIntentForDelayedSms(message.id)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, sendTime, intent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, sendTime, intent)
            }
        } else {
            val message = insertSentSms(subId, threadId, address, body, System.currentTimeMillis())
            sendSms(message)
        }
    }

    override fun sendSms(message: Message) {
        val smsManager = message.subId.takeIf { it != -1 }
                ?.let(SmsManager::getSmsManagerForSubscriptionId)
                ?: SmsManager.getDefault()

        val parts = smsManager.divideMessage(if (prefs.unicode.get()) StripAccents.stripAccents(message.body) else message.body)
                ?: arrayListOf()

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

    override fun sendMms(subId: Int, threadId: Long, addresses: List<String>, body: String, attachments: List<Attachment>) {
        val settings = Settings().apply { subscriptionId = subId }
        val message = com.klinker.android.send_message.Message(body, addresses.toTypedArray())

        // Add the GIFs as attachments. The app currently can't compress them, which may result
        // in a lot of these messages failing to send
        // TODO Add support for GIF compression
        attachments
                .filter { attachment -> attachment.isGif(context) }
                .map { attachment -> attachment.getUri() }
                .map { uri -> context.contentResolver.openInputStream(uri) }
                .map { inputStream -> inputStream.readBytes() }
                .forEach { bitmap -> message.addMedia(bitmap, ContentType.IMAGE_GIF) }

        // Compress the images and add them as attachments
        var totalImageBytes = 0
        attachments
                .filter { attachment -> !attachment.isGif(context) }
                .mapNotNull { attachment -> attachment.getUri() }
                .mapNotNull { uri -> imageRepository.loadImage(uri) }
                .also { totalImageBytes = it.sumBy { it.allocationByteCount } }
                .map { bitmap ->
                    val byteRatio = bitmap.allocationByteCount / totalImageBytes.toFloat()
                    shrink(bitmap, (prefs.mmsSize.get() * 1024 * byteRatio).toInt())
                }
                .forEach { bitmap -> message.addMedia(bitmap, ContentType.IMAGE_JPEG) }

        val transaction = Transaction(context, settings)
        transaction.sendNewMessage(message, threadId)
    }

    private fun shrink(src: Bitmap, maxBytes: Int): ByteArray {
        var step = 0.0
        val factor = 0.5
        val quality = 60

        val height = src.height
        val width = src.width

        val stream = ByteArrayOutputStream()
        src.compress(Bitmap.CompressFormat.JPEG, 100, stream)

        while (maxBytes > 0 && stream.size() > maxBytes) {
            step++
            val scale = Math.pow(factor, step)

            stream.reset()
            Bitmap.createScaledBitmap(src, (width * scale).toInt(), (height * scale).toInt(), false)
                    .compress(Bitmap.CompressFormat.JPEG, quality, stream)
        }

        return stream.toByteArray()
    }

    override fun cancelDelayedSms(id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getIntentForDelayedSms(id))
    }

    private fun getIntentForDelayedSms(id: Long): PendingIntent {
        val action = "com.moez.QKSMS.SEND_SMS"
        val intent = Intent(action).putExtra("id", id)
        BroadcastUtils.addClassName(context, intent, action)
        return PendingIntent.getBroadcast(context, id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun insertSentSms(subId: Int, threadId: Long, address: String, body: String, date: Long): Message {

        // Insert the message to Realm
        val message = Message().apply {
            this.threadId = threadId
            this.address = address
            this.body = body
            this.date = date
            this.subId = subId

            id = messageIds.newId()
            boxId = Telephony.Sms.MESSAGE_TYPE_OUTBOX
            type = "sms"
            read = true
            seen = true
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
            put(Telephony.Sms.SUBSCRIPTION_ID, subId)
        }
        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)

        // Update the contentId after the message has been inserted to the content provider
        // The message might have been deleted by now, so only proceed if it's valid
        //
        // We do this after inserting the message because it might be slow, and we want the message
        // to be inserted into Realm immediately. We don't need to do this after receiving one
        realm.executeTransaction { managedMessage?.takeIf { it.isValid }?.contentId = uri.lastPathSegment.toLong() }
        realm.close()

        return message
    }

    override fun insertReceivedSms(subId: Int, address: String, body: String, sentTime: Long): Message {

        // Insert the message to the native content provider
        val uri = contentValuesOf(
                Pair(Telephony.Sms.ADDRESS, address),
                Pair(Telephony.Sms.BODY, body),
                Pair(Telephony.Sms.DATE_SENT, sentTime),
                Pair(Telephony.Sms.SUBSCRIPTION_ID, subId))
                .let { values -> context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values) }

        // Insert the message to Realm
        return Message().apply {
            this.address = address
            this.body = body
            this.dateSent = sentTime
            this.date = System.currentTimeMillis()
            this.subId = subId

            id = messageIds.newId()
            threadId = TelephonyCompat.getOrCreateThreadId(context, address)
            contentId = uri?.lastPathSegment?.toLong() ?: 0L
            boxId = Telephony.Sms.MESSAGE_TYPE_INBOX
            type = "sms"
            insertOrUpdate()
        }
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

    override fun deleteMessages(vararg messageIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val messages = realm.where(Message::class.java)
                    .anyOf("id", messageIds)
                    .findAll()

            val uris = messages.map { it.getUri() }

            realm.executeTransaction { messages.deleteAllFromRealm() }

            uris.forEach { uri -> context.contentResolver.delete(uri, null, null) }
        }
    }

}