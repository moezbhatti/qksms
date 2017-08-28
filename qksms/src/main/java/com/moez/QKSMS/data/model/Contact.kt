package com.moez.QKSMS.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Contact() : RealmObject() {

    @PrimaryKey var id: Long = 0
    var address: String = ""

    /**
     * For inflating the contact using the cursor
     */
    constructor(id: Long, address: String) : this() {
        this.id = id
        this.address = address
    }

}