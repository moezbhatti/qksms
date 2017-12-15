package com.moez.QKSMS.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Recipient : RealmObject() {

    @PrimaryKey var id: Long = 0
    var address: String = ""
    var contact: Contact? = null
    var lastUpdate: Long = 0

}