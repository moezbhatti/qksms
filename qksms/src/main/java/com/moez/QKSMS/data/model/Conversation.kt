package com.moez.QKSMS.data.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Conversation : RealmObject() {

    @PrimaryKey var id: Long = 0
    var contacts: RealmList<Contact> = RealmList()

    fun getTitle(): String {
        var title = ""
        contacts.forEachIndexed { index, recipient ->
            title += if (recipient.name.isNotEmpty()) recipient.name else recipient.address
            if (index < contacts.size - 1) {
                title += ", "
            }
        }

        return title
    }
}
