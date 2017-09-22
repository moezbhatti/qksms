package com.moez.QKSMS.data.repository

import com.moez.QKSMS.data.model.Conversation
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

class ConversationRepository {

    fun getConversationAsync(threadId: Long): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("id", threadId)
                .findAllAsync()
    }

    fun getConversationsAsync(): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .findAllSortedAsync("date", Sort.DESCENDING)
    }

    fun getUnreadConversations(): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("read", false)
                .findAllSorted("date", Sort.DESCENDING)
    }

}