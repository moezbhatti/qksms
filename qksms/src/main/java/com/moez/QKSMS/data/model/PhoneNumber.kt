package com.moez.QKSMS.data.model

import io.realm.RealmObject

open class PhoneNumber : RealmObject() {

    var address: String = ""
    var label: String = ""

}