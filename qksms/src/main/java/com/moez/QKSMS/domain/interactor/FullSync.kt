package com.moez.QKSMS.domain.interactor

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.mapper.CursorToConversation
import com.moez.QKSMS.data.mapper.CursorToMessage
import io.reactivex.Flowable
import io.realm.Realm
import javax.inject.Inject

class FullSync @Inject constructor(
        private val context: Context,
        private val cursorToConversation: CursorToConversation,
        private val cursorToMessage: CursorToMessage)
    : Interactor<Long, Unit>() {

    override fun buildObservable(params: Unit): Flowable<Long> {
        val contentResolver = context.contentResolver
        val conversationsCursor = contentResolver.query(CursorToConversation.URI, CursorToConversation.PROJECTION, null, null, "date desc")

        var realm: Realm? = null

        return Flowable.just(conversationsCursor)
                .doOnNext {
                    // We need to set up realm on the io thread, and doOnSubscribe doesn't support setting a custom Scheduler
                    realm = Realm.getDefaultInstance()
                    realm?.beginTransaction()
                    realm?.deleteAll()
                }
                .flatMap { cursor -> cursor.asFlowable().map { cursorToConversation.map(cursor) } }
                .distinct { conversation -> conversation.id }
                .doOnNext { conversation -> realm?.insertOrUpdate(conversation) }
                .map { conversation -> conversation.id }
                .map { threadId -> Uri.withAppendedPath(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, threadId.toString()) }
                .map { threadUri -> contentResolver.query(threadUri, CursorToMessage.CURSOR_PROJECTION, null, null, "date desc") }
                .flatMap { cursor ->
                    val columnsMap = CursorToMessage.MessageColumns(cursor)
                    cursor.asFlowable().map { cursorToMessage.map(Pair(cursor, columnsMap)) }
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
    }

}