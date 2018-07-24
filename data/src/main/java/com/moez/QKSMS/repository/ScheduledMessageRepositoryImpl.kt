/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.repository

import com.moez.QKSMS.model.ScheduledMessage
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import javax.inject.Inject

class ScheduledMessageRepositoryImpl @Inject constructor() : ScheduledMessageRepository {

    override fun saveScheduledMessage(date: Long, subId: Int, recipients: List<String>, sendAsGroup: Boolean,
                                      body: String, attachments: List<String>) {

        Realm.getDefaultInstance().use { realm ->
            val id = (realm.where(ScheduledMessage::class.java).max("id")?.toLong() ?: -1) + 1
            val recipientsRealmList = RealmList(*recipients.toTypedArray())
            val attachmentsRealmList = RealmList(*attachments.toTypedArray())

            val message = ScheduledMessage(id, date, subId, recipientsRealmList, sendAsGroup, body, attachmentsRealmList)

            realm.executeTransaction { realm.insertOrUpdate(message) }
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
                .apply { refresh() }
                .where(ScheduledMessage::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    override fun deleteScheduledMessage(id: Long) {
        Realm.getDefaultInstance()
                .apply { refresh() }
                .use { realm ->
                    val message = realm.where(ScheduledMessage::class.java)
                            .equalTo("id", id)
                            .findFirst()

                    realm.executeTransaction { message?.deleteFromRealm() }
                }
    }

}