package com.moez.QKSMS

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.moez.QKSMS.dagger.AppComponent
import com.moez.QKSMS.dagger.AppModule
import com.moez.QKSMS.dagger.ConversationsModule
import com.moez.QKSMS.dagger.DaggerAppComponent
import io.realm.Realm
import io.realm.RealmConfiguration


class QKApplication : Application() {

    var appComponent: AppComponent? = null
        private set

    override fun onCreate() {
        super.onCreate()

        Bugsnag.init(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build())

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .conversationsModule(ConversationsModule())
                .build()
    }

}