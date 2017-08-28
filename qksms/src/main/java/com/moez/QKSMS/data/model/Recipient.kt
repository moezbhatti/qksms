package com.moez.QKSMS.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Recipient() : RealmObject() {

    @PrimaryKey var id: Long = 0

    /**
     * For inflating the recipient using just the id
     */
    constructor(id: Long) : this() {
        this.id = id
    }

}