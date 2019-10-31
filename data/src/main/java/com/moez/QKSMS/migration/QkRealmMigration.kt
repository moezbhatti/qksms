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
package com.moez.QKSMS.migration

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import io.realm.Sort

class QkRealmMigration : RealmMigration {

    companion object {
        const val SCHEMA_VERSION: Long = 9
    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var version = oldVersion

        if (version == 0L) {
            realm.schema.get("MmsPart")
                    ?.removeField("image")

            version++
        }

        if (version == 1L) {
            realm.schema.get("Message")
                    ?.addField("subId", Int::class.java)

            version++
        }

        if (version == 2L) {
            realm.schema.get("Conversation")
                    ?.addField("name", String::class.java, FieldAttribute.REQUIRED)

            version++
        }

        if (version == 3L) {
            realm.schema.create("ScheduledMessage")
                    .addField("id", Long::class.java, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
                    .addField("date", Long::class.java, FieldAttribute.REQUIRED)
                    .addField("subId", Long::class.java, FieldAttribute.REQUIRED)
                    .addRealmListField("recipients", String::class.java)
                    .addField("sendAsGroup", Boolean::class.java, FieldAttribute.REQUIRED)
                    .addField("body", String::class.java, FieldAttribute.REQUIRED)
                    .addRealmListField("attachments", String::class.java)

            version++
        }

        if (version == 4L) {
            realm.schema.get("Conversation")
                    ?.addField("pinned", Boolean::class.java, FieldAttribute.REQUIRED, FieldAttribute.INDEXED)

            version++
        }

        if (version == 5L) {
            realm.schema.create("BlockedNumber")
                    .addField("id", Long::class.java, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
                    .addField("address", String::class.java, FieldAttribute.REQUIRED)

            version++
        }

        if (version == 6L) {
            realm.schema.get("Conversation")
                    ?.addField("blockingClient", Integer::class.java)
                    ?.addField("blockReason", String::class.java)

            realm.schema.get("MmsPart")
                    ?.addField("seq", Integer::class.java, FieldAttribute.REQUIRED)
                    ?.addField("name", String::class.java)

            version++
        }

        if (version == 7L) {
            realm.schema.get("Conversation")
                    ?.addRealmObjectField("lastMessage", realm.schema.get("Message"))
                    ?.removeField("count")
                    ?.removeField("date")
                    ?.removeField("snippet")
                    ?.removeField("read")
                    ?.removeField("me")

            val conversations = realm.where("Conversation")
                    .findAll()

            val messages = realm.where("Message")
                    .sort("date", Sort.DESCENDING)
                    .distinct("threadId")
                    .findAll()
                    .associateBy { message -> message.getLong("threadId") }

            conversations.forEach { conversation ->
                conversation.setObject("lastMessage", messages[conversation.getLong("id")])
            }

            version++
        }

        if (version == 8L) {
            realm.schema.create("ContactGroup")
                    .addField("id", Long::class.java, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
                    .addField("title", String::class.java, FieldAttribute.REQUIRED)
                    .addRealmListField("contacts", realm.schema.get("Contact"))

            realm.schema.get("Contact")
                    ?.addField("starred", Boolean::class.java, FieldAttribute.REQUIRED)

            realm.schema.get("PhoneNumber")
                    ?.addField("accountType", String::class.java, FieldAttribute.REQUIRED)

            version++
        }

        check(version >= newVersion) { "Migration missing from v$oldVersion to v$newVersion" }
    }

}
