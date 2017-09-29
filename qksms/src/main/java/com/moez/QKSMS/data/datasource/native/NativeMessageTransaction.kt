package com.moez.QKSMS.data.datasource.native

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.Telephony
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.datasource.MessageTransaction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

class NativeMessageTransaction @Inject @Named("Native") constructor(private val context: Context) : MessageTransaction {

    override fun markSeen() {
        // TODO also need to mark MMS in ContentProvider as Seen
        val projection = arrayOf(BaseColumns._ID)
        val selection = "${Telephony.Sms.SEEN} = 0"
        val contentResolver = context.contentResolver
        contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, projection, selection, null, null)
                .asFlowable()
                .subscribeOn(Schedulers.io())
                .map { cursor -> cursor.getLong(0) }
                .map { id -> Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, id.toString()) }
                .subscribe { uri ->
                    val values = ContentValues()
                    values.put(Telephony.Sms.SEEN, true)
                    contentResolver.update(uri, values, null, null)
                }
    }

    override fun markSeen(threadId: Long) {
        // TODO also need to mark MMS in ContentProvider as Seen
        val projection = arrayOf(BaseColumns._ID)
        val selection = "${Telephony.Sms.THREAD_ID} = $threadId AND ${Telephony.Sms.SEEN} = 0"
        val contentResolver = context.contentResolver
        contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, projection, selection, null, null)
                .asFlowable()
                .subscribeOn(Schedulers.io())
                .map { cursor -> cursor.getLong(0) }
                .map { id -> Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, id.toString()) }
                .subscribe { uri ->
                    val values = ContentValues()
                    values.put(Telephony.Sms.SEEN, true)
                    contentResolver.update(uri, values, null, null)
                }
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
    }

    override fun markSent(id: Long) {
    }

    override fun markFailed(id: Long) {
    }

}