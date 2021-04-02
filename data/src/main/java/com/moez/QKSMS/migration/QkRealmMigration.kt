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

import android.annotation.SuppressLint
import com.moez.QKSMS.extensions.map
import com.moez.QKSMS.mapper.CursorToContactImpl
import com.moez.QKSMS.util.Preferences
import io.realm.DynamicRealm
import io.realm.DynamicRealmObject
import io.realm.FieldAttribute
import io.realm.RealmList
import io.realm.RealmMigration
import io.realm.Sort
import javax.inject.Inject

class QkRealmMigration @Inject constructor(
    private val cursorToContact: CursorToContactImpl,
    private val prefs: Preferences
) : RealmMigration {

    companion object {
        const val SchemaVersion: Long = 11
    }

    @SuppressLint("ApplySharedPref")
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
            // Delete this data since we'll need to repopulate it with its new primaryKey
            realm.delete("PhoneNumber")

            realm.schema.create("ContactGroup")
                    .addField("id", Long::class.java, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
                    .addField("title", String::class.java, FieldAttribute.REQUIRED)
                    .addRealmListField("contacts", realm.schema.get("Contact"))

            realm.schema.get("PhoneNumber")
                    ?.addField("id", Long::class.java, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
                    ?.addField("accountType", String::class.java)
                    ?.addField("isDefault", Boolean::class.java, FieldAttribute.REQUIRED)

            val phoneNumbers = cursorToContact.getContactsCursor()
                    ?.map(cursorToContact::map)
                    ?.distinctBy { contact -> contact.numbers.firstOrNull()?.id } // Each row has only one number
                    ?.groupBy { contact -> contact.lookupKey }
                    ?: mapOf()

            realm.schema.get("Contact")
                    ?.addField("starred", Boolean::class.java, FieldAttribute.REQUIRED)
                    ?.addField("photoUri", String::class.java)
                    ?.transform { realmContact ->
                        val numbers = RealmList<DynamicRealmObject>()
                        phoneNumbers[realmContact.get("lookupKey")]
                                ?.flatMap { contact -> contact.numbers }
                                ?.map { number ->
                                    realm.createObject("PhoneNumber", number.id).apply {
                                        setString("accountType", number.accountType)
                                        setString("address", number.address)
                                        setString("type", number.type)
                                    }
                                }
                                ?.let(numbers::addAll)

                        val photoUri = phoneNumbers[realmContact.get("lookupKey")]
                                ?.firstOrNull { number -> number.photoUri != null }
                                ?.photoUri

                        realmContact.setList("numbers", numbers)
                        realmContact.setString("photoUri", photoUri)
                    }

            // Migrate conversation themes
            val recipients = mutableMapOf<Long, Int>() // Map of recipientId:theme
            realm.where("Conversation").findAll().forEach { conversation ->
                val pref = prefs.theme(conversation.getLong("id"))
                if (pref.isSet) {
                    conversation.getList("recipients").forEach { recipient ->
                        recipients[recipient.getLong("id")] = pref.get()
                    }

                    pref.delete()
                }
            }

            recipients.forEach { (recipientId, theme) ->
                prefs.theme(recipientId).set(theme)
            }

            version++
        }

        if (version == 9L) {
            val migrateNotificationAction = { pref: Int ->
                when (pref) {
                    1 -> Preferences.NOTIFICATION_ACTION_READ
                    2 -> Preferences.NOTIFICATION_ACTION_REPLY
                    3 -> Preferences.NOTIFICATION_ACTION_CALL
                    4 -> Preferences.NOTIFICATION_ACTION_DELETE
                    else -> pref
                }
            }

            val migrateSwipeAction = { pref: Int ->
                when (pref) {
                    2 -> Preferences.SWIPE_ACTION_DELETE
                    3 -> Preferences.SWIPE_ACTION_CALL
                    4 -> Preferences.SWIPE_ACTION_READ
                    5 -> Preferences.SWIPE_ACTION_UNREAD
                    else -> pref
                }
            }

            if (prefs.notifAction1.isSet) prefs.notifAction1.set(migrateNotificationAction(prefs.notifAction1.get()))
            if (prefs.notifAction2.isSet) prefs.notifAction2.set(migrateNotificationAction(prefs.notifAction2.get()))
            if (prefs.notifAction3.isSet) prefs.notifAction3.set(migrateNotificationAction(prefs.notifAction3.get()))
            if (prefs.swipeLeft.isSet) prefs.swipeLeft.set(migrateSwipeAction(prefs.swipeLeft.get()))
            if (prefs.swipeRight.isSet) prefs.swipeRight.set(migrateSwipeAction(prefs.swipeRight.get()))

            version++
        }

        if (version == 10L) {
            realm.schema.get("MmsPart")
                    ?.addField("messageId", Long::class.java, FieldAttribute.INDEXED, FieldAttribute.REQUIRED)
                    ?.transform { part ->
                        val messageId = part.linkingObjects("Message", "parts").firstOrNull()?.getLong("contentId") ?: 0
                        part.setLong("messageId", messageId)
                    }

            version++
        }

        check(version >= newVersion) { "Migration missing from v$oldVersion to v$newVersion" }
    }

}
