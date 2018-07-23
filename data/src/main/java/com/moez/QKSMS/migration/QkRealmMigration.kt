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


class QkRealmMigration : RealmMigration {

    companion object {
        const val SCHEMA_VERSION: Long = 5
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

        if (version < newVersion) {
            throw IllegalStateException("Migration missing from v$oldVersion to v$newVersion")
        }
    }

}