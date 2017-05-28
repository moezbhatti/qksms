package com.moez.QKSMS

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class QKApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build())
    }

}