package com.moez.QKSMS.data.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Contact : RealmObject() {

    @PrimaryKey var lookupKey: String = ""
    var numbers: RealmList<PhoneNumber> = RealmList()
    var name: String = ""
    var lastUpdate: Long = 0

}