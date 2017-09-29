package com.moez.QKSMS.common.di

import com.moez.QKSMS.common.QKApplication

internal object AppComponentManager {

    internal lateinit var appComponent: AppComponent
        private set

    fun init(application: QKApplication) {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(application))
                .build()
    }

}