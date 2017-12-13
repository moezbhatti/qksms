package com.moez.QKSMS.domain.interactor

import android.content.Context
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.mapper.CursorToConversation
import com.moez.QKSMS.data.mapper.CursorToMessage
import com.moez.QKSMS.data.mapper.CursorToRecipient
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
        private val cursorToMessage: CursorToMessage,
        private val cursorToRecipient: CursorToRecipient)
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
                .flatMap {
                    // Sync conversations
                    contentResolver.query(CursorToConversation.URI, CursorToConversation.PROJECTION,
                            "date >= ?", arrayOf(lastSync.toString()), "date desc")
                            .asFlowable()
                            .map { cursor -> cursorToConversation.map(cursor) }
                            .toList().toFlowable()
                            .doOnNext { conversations -> realm?.insertOrUpdate(conversations) }
                }
                .flatMap {
                    // Sync messages
                    val messageCursor = contentResolver.query(CursorToMessage.URI, CursorToMessage.PROJECTION, null, null, "normalized_date desc")
                    val messageColumns = CursorToMessage.MessageColumns(messageCursor)
                    messageCursor.asFlowable()
                            .map { cursor -> cursorToMessage.map(Pair(cursor, messageColumns)) }
                            .takeWhile { message -> message.date >= lastSync }
                            .filter { message -> message.type == "sms" || message.type == "mms" }
                            .toList().toFlowable()
                            .doOnNext { messages -> realm?.insertOrUpdate(messages) }
                }
                .flatMap {
                    // Sync recipients
                    contentResolver.query(CursorToRecipient.URI, null, null, null, null).asFlowable()
                            .map { cursor -> cursorToRecipient.map(cursor) }
                            .toList().toFlowable()
                            .doOnNext { recipients -> realm?.insertOrUpdate(recipients) }
                }
                .doOnNext {
                    realm?.commitTransaction()
                    realm?.close()
                    Timber.v("Performed sync in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)} seconds")
                }
                .map { 0L }
    }

}