package com.moez.QKSMS.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.moez.QKSMS.data.model.Message
import io.realm.Realm
import io.realm.RealmResults
import timber.log.Timber

class MessageRepository(val context: Context) {

    fun getMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .findAllSortedAsync("date")
    }

    fun insertMessage(address: String, body: String, time: Long) {
        val contentResolver = context.contentResolver
        val cv = ContentValues()

        cv.put("address", address)
        cv.put("body", body)
        cv.put("date_sent", time)

        val uri = contentResolver.insert(Uri.parse("content://sms/inbox"), cv)

        Timber.d("Uri: $uri")
    }

}