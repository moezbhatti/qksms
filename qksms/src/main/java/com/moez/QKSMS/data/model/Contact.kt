package com.moez.QKSMS.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Contact() : RealmObject() {

    @PrimaryKey var recipientId: Long = 0
    var address: String = ""
    var name: String = ""

    constructor(id: Long, address: String) : this() {
        this.recipientId = id
        this.address = address
    }

}