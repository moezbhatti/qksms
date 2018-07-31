/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.extensions

import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.RealmResults

fun RealmModel.insertOrUpdate() {
    val realm = Realm.getDefaultInstance()
    realm.executeTransaction { realm.insertOrUpdate(this) }
    realm.close()
}

fun <T : RealmModel> Collection<T>.insertOrUpdate() {
    val realm = Realm.getDefaultInstance()
    realm.executeTransaction { realm.insertOrUpdate(this) }
    realm.close()
}

fun <T : RealmObject> T.asObservable(): Observable<T> {
    return asFlowable<T>().toObservable()
}

fun <T : RealmObject> RealmResults<T>.asObservable(): Observable<RealmResults<T>> {
    return asFlowable().toObservable()
}

fun <T : RealmObject> RealmQuery<T>.anyOf(fieldName: String, values: LongArray): RealmQuery<T> {
    return when (values.isEmpty()) {
        true -> equalTo(fieldName, -1L)
        false -> `in`(fieldName, values.toTypedArray())
    }
}