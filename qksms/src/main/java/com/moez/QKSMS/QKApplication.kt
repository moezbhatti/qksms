package com.moez.QKSMS

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.moez.QKSMS.dagger.*
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
                .contactModule(ContactModule())
                .conversationsModule(ConversationsModule())
                .build()
    }

}