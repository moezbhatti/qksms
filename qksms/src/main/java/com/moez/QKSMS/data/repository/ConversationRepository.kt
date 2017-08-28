package com.moez.QKSMS.data.repository

import com.moez.QKSMS.data.model.Conversation
import io.realm.Realm
import io.realm.RealmResults

class ConversationRepository {

    fun getConversation(threadId: Long): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("recipientId", threadId)
                .findAllAsync()
    }

    fun getConversations(): RealmResults<Conversation> {
        return Realm.getDefaultInstance().where(Conversation::class.java).findAllAsync()
    }

}