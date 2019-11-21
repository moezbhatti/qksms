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
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.Telephony
import android.telephony.SmsManager
import android.webkit.MimeTypeMap
import androidx.core.content.contentValuesOf
import com.google.android.mms.ContentType
import com.google.android.mms.MMSPart
import com.google.android.mms.pdu_alt.MultimediaMessagePdu
import com.google.android.mms.pdu_alt.PduPersister
import com.klinker.android.send_message.SmsManagerFactory
import com.klinker.android.send_message.StripAccents
import com.klinker.android.send_message.Transaction
import com.moez.QKSMS.compat.TelephonyCompat
import com.moez.QKSMS.extensions.anyOf
import com.moez.QKSMS.manager.ActiveConversationManager
import com.moez.QKSMS.manager.KeyManager
import com.moez.QKSMS.model.Attachment
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.MmsPart
import com.moez.QKSMS.receiver.SendSmsReceiver
import com.moez.QKSMS.receiver.SmsDeliveredReceiver
import com.moez.QKSMS.receiver.SmsSentReceiver
import com.moez.QKSMS.util.ImageUtils
import com.moez.QKSMS.util.Preferences
import com.moez.QKSMS.util.tryOrNull
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val activeConversationManager: ActiveConversationManager,
    private val context: Context,
    private val imageRepository: ImageRepository,
    private val messageIds: KeyManager,
    private val prefs: Preferences,
    private val syncRepository: SyncRepository
) : MessageRepository {

    override fun getMessages(threadId: Long, query: String): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .let {
                    when (query.isEmpty()) {
                        true -> it
                        false -> it
                                .beginGroup()
                                .contains("body", query, Case.INSENSITIVE)
                                .or()
                                .contains("parts.text", query, Case.INSENSITIVE)
                                .endGroup()
                    }
                }
                .sort("date")
                .findAllAsync()
    }

    override fun getMessage(id: Long): Message? {
        return Realm.getDefaultInstance()
                .also { realm -> realm.refresh() }
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
        return Realm.getDefaultInstance().use { realm ->
            realm.refresh()
            realm.where(Conversation::class.java)
                    .equalTo("archived", false)
                    .equalTo("blocked", false)
                    .equalTo("lastMessage.read", false)
                    .count()
        }
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

    override fun savePart(id: Long): File? {
        val part = getPart(id) ?: return null

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(part.type) ?: return null
        val date = part.messages?.first()?.date
        val dir = File(Environment.getExternalStorageDirectory(), "QKSMS/Media").apply { mkdirs() }
        val fileName = part.name?.takeIf { name -> name.endsWith(extension) }
                ?: "${part.type.split("/").last()}_$date.$extension"
        var file: File
        var index = 0
        do {
            file = File(dir, if (index == 0) fileName else fileName.replace(".$extension", " ($index).$extension"))
            index++
        } while (file.exists())

        try {
            FileOutputStream(file).use { outputStream ->
                context.contentResolver.openInputStream(part.getUri())?.use { inputStream ->
                    inputStream.copyTo(outputStream, 1024)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        MediaScannerConnection.scanFile(context, arrayOf(file.path), null, null)

        return file.takeIf { it.exists() }
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
            val conversations = realm.where(Conversation::class.java)
                    .anyOf("id", threadIds)
                    .equalTo("lastMessage.read", true)
                    .findAll()

            realm.executeTransaction {
                conversations.forEach { conversation ->
                    conversation.lastMessage?.read = false
                }
            }
        }
    }

    override fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>,
        delay: Int
    ) {
        val signedBody = when {
            prefs.signature.get().isEmpty() -> body
            body.isNotEmpty() -> body + '\n' + prefs.signature.get()
            else -> prefs.signature.get()
        }

        if (addresses.size == 1 && attachments.isEmpty()) { // SMS
            if (delay > 0) { // With delay
                val sendTime = System.currentTimeMillis() + delay
                val message = insertSentSms(subId, threadId, addresses.first(), signedBody, sendTime)

                val intent = getIntentForDelayedSms(message.id)

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, sendTime, intent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, sendTime, intent)
                }
            } else { // No delay
                val message = insertSentSms(subId, threadId, addresses.first(), signedBody, System.currentTimeMillis())
                sendSms(message)
            }
        } else { // MMS
            val parts = arrayListOf<MMSPart>()

            if (signedBody.isNotBlank()) {
                parts += MMSPart("text", ContentType.TEXT_PLAIN, signedBody.toByteArray())
            }

            val smsManager = subId.takeIf { it != -1 }
                    ?.let(SmsManagerFactory::createSmsManager)
                    ?: SmsManager.getDefault()
            val width = smsManager.carrierConfigValues.getInt(SmsManager.MMS_CONFIG_MAX_IMAGE_WIDTH)
            val height = smsManager.carrierConfigValues.getInt(SmsManager.MMS_CONFIG_MAX_IMAGE_HEIGHT)

            // Add the GIFs as attachments
            parts += attachments
                    .mapNotNull { attachment -> attachment as? Attachment.Image }
                    .filter { attachment -> attachment.isGif(context) }
                    .mapNotNull { attachment -> attachment.getUri() }
                    .map { uri -> ImageUtils.compressGif(context, uri, prefs.mmsSize.get() * 1024) }
                    .map { bitmap -> MMSPart("image", ContentType.IMAGE_GIF, bitmap) }

            // Compress the images and add them as attachments
            var totalImageBytes = 0
            parts += attachments
                    .mapNotNull { attachment -> attachment as? Attachment.Image }
                    .filter { attachment -> !attachment.isGif(context) }
                    .mapNotNull { attachment -> attachment.getUri() }
                    .mapNotNull { uri -> tryOrNull { imageRepository.loadImage(uri, width, height) } }
                    .also { totalImageBytes = it.sumBy { it.allocationByteCount } }
                    .map { bitmap ->
                        val byteRatio = bitmap.allocationByteCount / totalImageBytes.toFloat()
                        ImageUtils.compressBitmap(bitmap, (prefs.mmsSize.get() * 1024 * byteRatio).toInt())
                    }
                    .map { bitmap -> MMSPart("image", ContentType.IMAGE_JPEG, bitmap) }

            // Send contacts
            parts += attachments
                    .mapNotNull { attachment -> attachment as? Attachment.Contact }
                    .map { attachment -> attachment.vCard.toByteArray() }
                    .map { vCard -> MMSPart("contact", ContentType.TEXT_VCARD, vCard) }

            Transaction(context).sendNewMessage(subId, threadId, addresses, parts, null, null)
        }
    }

    override fun sendSms(message: Message) {
        val smsManager = message.subId.takeIf { it != -1 }
                ?.let(SmsManagerFactory::createSmsManager)
                ?: SmsManager.getDefault()

        val parts = smsManager
                .divideMessage(if (prefs.unicode.get()) StripAccents.stripAccents(message.body) else message.body)
                ?: arrayListOf()

        val sentIntents = parts.map {
            val intent = Intent(context, SmsSentReceiver::class.java).putExtra("id", message.id)
            PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val deliveredIntents = parts.map {
            val intent = Intent(context, SmsDeliveredReceiver::class.java).putExtra("id", message.id)
            val pendingIntent = PendingIntent
                    .getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
            if (prefs.delivery.get()) pendingIntent else null
        }

        try {
            smsManager.sendMultipartTextMessage(
                    message.address,
                    null,
                    parts,
                    ArrayList(sentIntents),
                    ArrayList(deliveredIntents)
            )
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "Message body lengths: ${parts.map { it?.length }}")
            markFailed(message.id, Telephony.MmsSms.ERR_TYPE_GENERIC)
        }
    }

    override fun resendMms(message: Message) {
        val subId = message.subId
        val threadId = message.threadId
        val pdu = tryOrNull {
            PduPersister.getPduPersister(context).load(message.getUri()) as MultimediaMessagePdu
        } ?: return

        val addresses = pdu.to.map { it.string }.filter { it.isNotBlank() }
        val parts = message.parts.mapNotNull { part ->
            val bytes = tryOrNull {
                context.contentResolver.openInputStream(part.getUri())?.use { inputStream -> inputStream.readBytes() }
            } ?: return@mapNotNull null

            MMSPart(part.name.orEmpty(), part.type, bytes)
        }

        Transaction(context).sendNewMessage(subId, threadId, addresses, parts, message.subject, message.getUri())
    }

    override fun cancelDelayedSms(id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getIntentForDelayedSms(id))
    }

    private fun getIntentForDelayedSms(id: Long): PendingIntent {
        val intent = Intent(context, SendSmsReceiver::class.java).putExtra("id", id)
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
        val values = contentValuesOf(
                Telephony.Sms.ADDRESS to address,
                Telephony.Sms.BODY to body,
                Telephony.Sms.DATE to System.currentTimeMillis(),
                Telephony.Sms.READ to true,
                Telephony.Sms.SEEN to true,
                Telephony.Sms.TYPE to Telephony.Sms.MESSAGE_TYPE_OUTBOX,
                Telephony.Sms.THREAD_ID to threadId
        )

        if (prefs.canUseSubId.get()) {
            values.put(Telephony.Sms.SUBSCRIPTION_ID, message.subId)
        }

        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)

        // Update the contentId after the message has been inserted to the content provider
        // The message might have been deleted by now, so only proceed if it's valid
        //
        // We do this after inserting the message because it might be slow, and we want the message
        // to be inserted into Realm immediately. We don't need to do this after receiving one
        uri?.lastPathSegment?.toLong()?.let { id ->
            realm.executeTransaction { managedMessage?.takeIf { it.isValid }?.contentId = id }
        }
        realm.close()

        // On some devices, we can't obtain a threadId until after the first message is sent in a
        // conversation. In this case, we need to update the message's threadId after it gets added
        // to the native ContentProvider
        if (threadId == 0L) {
            uri?.let(syncRepository::syncMessage)
        }

        return message
    }

    override fun insertReceivedSms(subId: Int, address: String, body: String, sentTime: Long): Message {

        // Insert the message to Realm
        val message = Message().apply {
            this.address = address
            this.body = body
            this.dateSent = sentTime
            this.date = System.currentTimeMillis()
            this.subId = subId

            id = messageIds.newId()
            threadId = TelephonyCompat.getOrCreateThreadId(context, address)
            boxId = Telephony.Sms.MESSAGE_TYPE_INBOX
            type = "sms"
            read = activeConversationManager.getActiveConversation() == threadId
        }
        val realm = Realm.getDefaultInstance()
        var managedMessage: Message? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

        // Insert the message to the native content provider
        val values = contentValuesOf(
                Telephony.Sms.ADDRESS to address,
                Telephony.Sms.BODY to body,
                Telephony.Sms.DATE_SENT to sentTime
        )

        if (prefs.canUseSubId.get()) {
            values.put(Telephony.Sms.SUBSCRIPTION_ID, message.subId)
        }

        context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)?.lastPathSegment?.toLong()?.let { id ->
            // Update the contentId after the message has been inserted to the content provider
            realm.executeTransaction { managedMessage?.contentId = id }
        }

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
                    message.boxId = when (message.isSms()) {
                        true -> Telephony.Sms.MESSAGE_TYPE_OUTBOX
                        false -> Telephony.Mms.MESSAGE_BOX_OUTBOX
                    }
                }

                // Update the message in the native ContentProvider
                val values = when (message.isSms()) {
                    true -> contentValuesOf(Telephony.Sms.TYPE to Telephony.Sms.MESSAGE_TYPE_OUTBOX)
                    false -> contentValuesOf(Telephony.Mms.MESSAGE_BOX to Telephony.Mms.MESSAGE_BOX_OUTBOX)
                }
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