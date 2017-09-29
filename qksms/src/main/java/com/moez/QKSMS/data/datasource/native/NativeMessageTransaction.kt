package com.moez.QKSMS.data.datasource.native

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.Telephony
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.datasource.MessageTransaction
import io.reactivex.schedulers.Schedulers

class NativeMessageTransaction(private val context: Context) : MessageTransaction {

    override fun markSent(id: Long) {
    }

    override fun markFailed(id: Long) {
    }

    override fun markSeen() {
    }

    override fun markSeen(threadId: Long) {
    }

    override fun markRead(threadId: Long) {
        // Messages in SMS ContentProvider
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

        // TODO also need to mark MMS in ContentProvider as Read
    }

}