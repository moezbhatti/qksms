package com.moez.QKSMS.repository

import com.moez.QKSMS.model.ScheduledMessage
import io.realm.RealmResults

interface ScheduledMessageRepository {

    /**
     * Saves a scheduled message
     */
    fun saveScheduledMessage(date: Long, subId: Int, recipients: List<String>, sendAsGroup: Boolean, body: String,
                             attachments: List<String>)

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