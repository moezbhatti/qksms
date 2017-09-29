package com.moez.QKSMS.common.util.extensions

import io.realm.Realm
import io.realm.RealmObject

fun RealmObject.insertOrUpdate() {
    val realm = Realm.getDefaultInstance()
    realm.executeTransaction { realm.insertOrUpdate(this) }
    realm.close()
}