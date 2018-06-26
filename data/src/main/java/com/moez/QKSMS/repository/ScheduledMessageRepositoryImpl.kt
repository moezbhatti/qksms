package com.moez.QKSMS.repository

import com.moez.QKSMS.model.ScheduledMessage
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults

class ScheduledMessageRepositoryImpl : ScheduledMessageRepository {

    override fun saveScheduledMessage(date: Long, recipients: RealmList<String>, sendAsGroup: Boolean, body: String,
                                      attachments: RealmList<String>): Long {

        return Realm.getDefaultInstance().use { realm ->
            val id = (realm.where(ScheduledMessage::class.java).max("id")?.toLong() ?: -1) + 1
            val message = ScheduledMessage(id, date, recipients, sendAsGroup, body, attachments)

            realm.executeTransaction { realm.insertOrUpdate(message) }

            id
        }
    }

    override fun getScheduledMessages(): RealmResults<ScheduledMessage> {
        return Realm.getDefaultInstance()
                .where(ScheduledMessage::class.java)
                .sort("date")
                .findAll()
    }

    override fun getScheduledMessage(id: Long): ScheduledMessage? {
        return Realm.getDefaultInstance()
                .where(ScheduledMessage::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    override fun deleteScheduledMessage(id: Long) {
        Realm.getDefaultInstance().use { realm ->
            val message = realm.where(ScheduledMessage::class.java)
                    .equalTo("id", id)
                    .findFirst()

            realm.executeTransaction { message?.deleteFromRealm() }
        }
    }

}