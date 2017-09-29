package com.moez.QKSMS.data.datasource.realm

import com.moez.QKSMS.data.datasource.MessageTransaction
import com.moez.QKSMS.data.model.Message
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealmMessageTransaction @Inject constructor() : MessageTransaction {

    override fun markSent(id: Long) {
    }

    override fun markFailed(id: Long) {
    }

    override fun markSeen() {
    }

    override fun markSeen(threadId: Long) {
    }

    override fun markRead(threadId: Long) {
        Schedulers.io().scheduleDirect {
            val realm = Realm.getDefaultInstance()
            val messages = realm.where(Message::class.java)
                    .equalTo("threadId", threadId)
                    .beginGroup()
                    .equalTo("read", false)
                    .or()
                    .equalTo("seen", false)
                    .endGroup()
                    .findAll()

            realm.executeTransaction {
                messages.forEach { message ->
                    message.seen = true
                    message.read = true
                }
            }
            realm.close()
        }
    }

}