package com.moez.QKSMS.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ScheduledMessage(
        @PrimaryKey var id: Long = 0,
        var date: Long = 0,
        var subId: Int = -1,
        var recipients: RealmList<String> = RealmList(),
        var sendAsGroup: Boolean = true,
        var body: String = "",
        var attachments: RealmList<String> = RealmList()
) : RealmObject() {

    fun copy(id: Long = this.id,
             date: Long = this.date,
             subId: Int = this.subId,
             recipients: RealmList<String> = this.recipients,
             sendAsGroup: Boolean = this.sendAsGroup,
             body: String = this.body,
             attachments: RealmList<String> = this.attachments): ScheduledMessage {

        return ScheduledMessage(id, date, subId, recipients, sendAsGroup, body, attachments)
    }

}