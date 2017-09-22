package com.moez.QKSMS.data.sync

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.ContactRepository
import com.moez.QKSMS.util.extensions.asFlowable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm

class SyncManager(val context: Context, private val contactsRepo: ContactRepository) {

    fun copyToRealm(completionListener: () -> Unit) {

        val contentResolver = context.contentResolver
        val conversationsCursor = contentResolver.query(ConversationColumns.URI, ConversationColumns.PROJECTION, null, null, "date desc")

        var realm: Realm? = null

        Flowable.just(conversationsCursor)
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    // We need to set up realm on the io thread, and doOnSubscribe doesn't support setting a custom Scheduler
                    realm = Realm.getDefaultInstance()
                    realm?.beginTransaction()
                    realm?.deleteAll()
                }
                .flatMap { cursor -> cursor.asFlowable() }
                .map { cursor -> conversationFromCursor(cursor) }
                .distinct { conversation -> conversation.id }
                .doOnNext { conversation -> realm?.insertOrUpdate(conversation) }
                .count()
                .toFlowable()
                .doOnNext {
                    realm?.commitTransaction()
                    realm?.close()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { completionListener.invoke() }
    }

    private fun conversationFromCursor(cursor: Cursor): Conversation {
        return Conversation().apply {
            id = cursor.getLong(ConversationColumns.ID)

            cursor.getString(ConversationColumns.RECIPIENT_IDS).split(" ")
                    .map { id -> id.toLong() }
                    .map { id -> contactsRepo.getContactBlocking(id) }
                    .filter { contact -> contact.recipientId != 0L }
                    .forEach { contact -> contacts.add(contact) }

            val contentResolver = context.contentResolver
            val uri = Uri.withAppendedPath(MessageColumns.URI, id.toString())
            val messagesCursor = contentResolver.query(uri, MessageColumns.PROJECTION, null, null, "date desc")
            val columnsMap = MessageColumns(messagesCursor)
            messagesCursor.asFlowable()
                    .map { cursor -> messageFromCursor(id, cursor, columnsMap) }
                    .filter { message -> message.type == "sms" || message.type == "mms" }
                    .distinct { message -> message.id }
                    .doOnNext { message -> messages.add(message) }
                    .blockingSubscribe()
        }
    }

    // TODO take this out of the companion object
    companion object {
        fun messageFromCursor(threadId: Long, cursor: Cursor, columnsMap: MessageColumns): Message {
            return Message().apply {
                this.threadId = threadId

                id = cursor.getLong(columnsMap.msgId)
                type = cursor.getString(columnsMap.msgType)

                val isMms = type == "mms"
                body = cursor.getString(columnsMap.smsBody) ?: ""
                errorType = cursor.getInt(columnsMap.mmsErrorType)

                if (isMms) {
                    boxId = cursor.getInt(columnsMap.mmsMessageBox)
                    date = cursor.getLong(columnsMap.smsDate)
                    dateSent = cursor.getLong(columnsMap.smsDateSent)
                    seen = cursor.getInt(columnsMap.smsSeen) != 0
                    read = cursor.getInt(columnsMap.smsRead) != 0
                } else {
                    boxId = cursor.getInt(columnsMap.smsType)
                    date = cursor.getLong(columnsMap.mmsDate)
                    dateSent = cursor.getLong(columnsMap.mmsDateSent)
                    seen = cursor.getInt(columnsMap.mmsSeen) != 0
                    read = cursor.getInt(columnsMap.mmsRead) != 0
                }
            }
        }
    }
}
