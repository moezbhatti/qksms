package com.moez.QKSMS

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.moez.QKSMS.dagger.AppComponentManager
import io.realm.Realm
import io.realm.RealmConfiguration


class QKApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Bugsnag.init(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build())

        AppComponentManager.init(this)
    }

}