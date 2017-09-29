package com.moez.QKSMS.data.datasource.realm

import com.moez.QKSMS.data.datasource.MessageTransaction
import com.moez.QKSMS.data.model.Message
import io.reactivex.schedulers.Schedulers
import io.realm.Realm

class RealmMessageTransaction : MessageTransaction {

    override fun markSeen() {
        Schedulers.io().scheduleDirect {
            val realm = Realm.getDefaultInstance()
            val messages = realm.where(Message::class.java).equalTo("seen", false).findAll()
            realm.executeTransaction { messages.forEach { message -> message.seen = true } }
            realm.close()
        }
    }

    override fun markSeen(threadId: Long) {
        Schedulers.io().scheduleDirect {
            val realm = Realm.getDefaultInstance()
            val messages = realm.where(Message::class.java)
                    .equalTo("threadId", threadId)
                    .equalTo("seen", false)
                    .findAll()

            realm.executeTransaction {
                messages.forEach { message ->
                    message.seen = true
                }
            }
            realm.close()
        }
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

    override fun markSent(id: Long) {
    }

    override fun markFailed(id: Long) {
    }

}