package com.moez.QKSMS.common.util

import android.arch.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmResults

class LiveRealmData<T : RealmModel>(private val results: RealmResults<T>) : LiveData<RealmResults<T>>() {

    private val listener: RealmChangeListener<RealmResults<T>> = RealmChangeListener { results ->
        value = results
    }

    override fun onActive() {
        super.onActive()
        results.addChangeListener(listener)
    }

    override fun onInactive() {
        super.onInactive()
        results.removeChangeListener(listener)
    }

}