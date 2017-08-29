package com.moez.QKSMS.dagger

import com.moez.QKSMS.QKApplication

internal object AppComponentManager {

    internal lateinit var appComponent: AppComponent
        private set

    fun init(application: QKApplication) {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(application))
                .contactModule(ContactModule())
                .conversationsModule(ConversationsModule())
                .build()
    }

}