package com.moez.QKSMS.data.model

import io.realm.RealmList
import io.realm.RealmObject

open class Contact  : RealmObject() {

    var lookupKey: String = ""
    var numbers: RealmList<PhoneNumber> = RealmList()
    var name: String = ""
    var lastUpdate: Long = 0

}