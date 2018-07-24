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