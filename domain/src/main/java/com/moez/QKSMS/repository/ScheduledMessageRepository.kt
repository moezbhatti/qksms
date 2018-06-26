package com.moez.QKSMS.repository

import com.moez.QKSMS.model.ScheduledMessage
import io.realm.RealmList
import io.realm.RealmResults

interface ScheduledMessageRepository {

    /**
     * Saves a scheduled message and returns the id if successful
     */
    fun saveScheduledMessage(date: Long, recipients: RealmList<String>, sendAsGroup: Boolean, body: String,
                             attachments: RealmList<String>): Long

    /**
     * Returns all of the scheduled messages, sorted chronologically
     */
    fun getScheduledMessages(): RealmResults<ScheduledMessage>

    /**
     * Returns the scheduled message with the given [id]
     */
    fun getScheduledMessage(id: Long): ScheduledMessage?

    /**
     * Deletes the scheduled message with the given [id]
     */
    fun deleteScheduledMessage(id: Long)

}