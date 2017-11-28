package com.moez.QKSMS.domain.interactor

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.mapper.CursorToConversation
import com.moez.QKSMS.data.mapper.CursorToMessage
import com.moez.QKSMS.data.model.SyncLog
import io.reactivex.Flowable
import io.realm.Realm
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// TODO: This needs to be substantially faster
open class PartialSync @Inject constructor(
        private val context: Context,
        private val cursorToConversation: CursorToConversation,
        private val cursorToMessage: CursorToMessage)
    : Interactor<Long, Unit>() {

    override fun buildObservable(params: Unit): Flowable<Long> {
        val contentResolver = context.contentResolver
        var realm: Realm? = null

        var lastSync = 0L
        var startTime = 0L

        return Flowable.just(params)
                .doOnNext {
                    startTime = System.currentTimeMillis()

                    // We need to set up realm on the io thread, and doOnSubscribe doesn't support setting a custom Scheduler
                    realm = Realm.getDefaultInstance()
                    realm?.beginTransaction()

                    lastSync = realm?.where(SyncLog::class.java)?.max("date")?.toLong() ?: 0

                    // Add a log entry for this sync
                    realm?.insert(SyncLog())
                }
                .map { contentResolver.query(CursorToConversation.URI, CursorToConversation.PROJECTION, "date >= ?", arrayOf(lastSync.toString()), "date desc") }
                .flatMap { cursor -> cursor.asFlowable().map { cursorToConversation.map(it) } }
                .distinct { conversation -> conversation.id }
                .doOnNext { conversation -> realm?.insertOrUpdate(conversation) }
                .map { conversation -> conversation.id }
                .map { threadId -> Uri.withAppendedPath(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, threadId.toString()) }
                .map { threadUri -> contentResolver.query(threadUri, CursorToMessage.CURSOR_PROJECTION, null, null, "date desc") }
                .flatMap { cursor ->
                    val columnsMap = CursorToMessage.MessageColumns(cursor)
                    cursor.asFlowable()
                            .map { cursorToMessage.map(Pair(cursor, columnsMap)) }
                            .takeWhile { message -> message.date >= lastSync }
                }
                .filter { message -> message.type == "sms" || message.type == "mms" }
                .distinct { message -> message.id }
                .doOnNext { message -> realm?.insertOrUpdate(message) }
                .count()
                .toFlowable()
                .doOnNext {
                    realm?.commitTransaction()
                    realm?.close()
                    Timber.v("Synced $it messages in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)} seconds")
                }
    }

}