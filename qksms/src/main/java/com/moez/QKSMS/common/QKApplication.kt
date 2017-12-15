package com.moez.QKSMS.common

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.moez.QKSMS.BuildConfig
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.Analytics
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber
import javax.inject.Inject


class QKApplication : Application() {

    /**
     * Inject this so that it is forced to initialize
     */
    @Suppress("unused")
    @Inject lateinit var analytics: Analytics

    override fun onCreate() {
        super.onCreate()

        Bugsnag.init(this, BuildConfig.BUGSNAG_API_KEY)

        AppComponentManager.init(this)
        appComponent.inject(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .compactOnLaunch()
                .deleteRealmIfMigrationNeeded()
                .build())

        Timber.plant(Timber.DebugTree())
    }

}