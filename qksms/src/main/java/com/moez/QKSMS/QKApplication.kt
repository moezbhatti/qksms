package com.moez.QKSMS

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.moez.QKSMS.dagger.AppComponentManager
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber


class QKApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Bugsnag.init(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .compactOnLaunch()
                .deleteRealmIfMigrationNeeded()
                .build())

        AppComponentManager.init(this)

        Timber.plant(Timber.DebugTree())
    }

}