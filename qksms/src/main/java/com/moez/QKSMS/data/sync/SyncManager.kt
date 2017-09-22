package com.moez.QKSMS.data.sync

import android.content.Context
import android.net.Uri
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.ContactRepository
import com.moez.QKSMS.util.extensions.asFlowable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm

class SyncManager(val context: Context, val contacts: ContactRepository) {
    private val TAG = "SyncManager"

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
                .map { cursor -> Conversation(cursor, contacts) }
                .distinct { conversation -> conversation.id }
                .doOnNext { conversation -> realm?.insertOrUpdate(conversation) }
                .map { conversation -> conversation.id }
                .concatMap { threadId ->
                    val uri = Uri.withAppendedPath(MessageColumns.URI, threadId.toString())
                    val messagesCursor = contentResolver.query(uri, MessageColumns.PROJECTION, null, null, "date desc")
                    val columnsMap = MessageColumns(messagesCursor)
                    messagesCursor.asFlowable().map { cursor -> Message(threadId, cursor, columnsMap) }
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

}
