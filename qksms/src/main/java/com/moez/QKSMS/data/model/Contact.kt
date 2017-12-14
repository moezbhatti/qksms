package com.moez.QKSMS.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Contact() : RealmObject() {

    @PrimaryKey var recipientId: Long = 0
    var lookupKey: String = ""
    var address: String = ""
    var name: String = ""

    var inContactsTable: Boolean = false

}