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
package data.model

import android.telephony.PhoneNumberUtils
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*

open class Conversation : RealmObject() {

    @PrimaryKey var id: Long = 0
    var recipients: RealmList<Recipient> = RealmList()
    @Index var archived: Boolean = false
    @Index var blocked: Boolean = false
    var draft: String = ""

    fun getTitle(): String {
        var title = ""
        recipients.forEachIndexed { index, recipient ->
            val name = recipient.contact?.name?.takeIf { it.isNotBlank() }
            title += name ?: PhoneNumberUtils.formatNumber(recipient.address, Locale.getDefault().country)
            if (index < recipients.size - 1) {
                title += ", "
            }
        }

        return title
    }
}
