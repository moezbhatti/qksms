package com.moez.QKSMS.common

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.moez.QKSMS.common.di.AppComponentManager
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber


class QKApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Bugsnag.init(this)

        MixpanelAPI.getInstance(this, "")

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .compactOnLaunch()
                .deleteRealmIfMigrationNeeded()
                .build())

        AppComponentManager.init(this)

        Timber.plant(Timber.DebugTree())
    }

}