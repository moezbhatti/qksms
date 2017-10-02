package com.moez.QKSMS.data.sync

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.mapper.CursorToMessageFlowable
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.ContactRepository
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(val context: Context, private val contactsRepo: ContactRepository) {

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
                .map { conversation -> conversation.id }
                .flatMap { threadId ->
                    val uri = Uri.withAppendedPath(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, threadId.toString())
                    val messagesCursor = contentResolver.query(uri, CursorToMessageFlowable.CURSOR_PROJECTION, null, null, "date desc")
                    CursorToMessageFlowable.map(messagesCursor)
                }
                .filter { message -> message.type == "sms" || message.type == "mms" }
                .distinct { message -> message.id }
                .doOnNext { message -> realm?.insertOrUpdate(message) }
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
        }
    }
}
