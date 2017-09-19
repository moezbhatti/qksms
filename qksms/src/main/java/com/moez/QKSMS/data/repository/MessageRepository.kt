package com.moez.QKSMS.data.repository

import com.moez.QKSMS.data.model.Message
import io.realm.Realm
import io.realm.RealmResults

class MessageRepository {

    fun getMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .findAllSortedAsync("date")
    }

}