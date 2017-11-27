package com.moez.QKSMS.data.model

import io.realm.RealmObject

open class SyncLog : RealmObject() {

    var date: Long = System.currentTimeMillis()

}