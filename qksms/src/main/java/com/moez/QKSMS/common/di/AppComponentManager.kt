package com.moez.QKSMS.common.di

import com.moez.QKSMS.common.QKApplication

internal lateinit var appComponent: AppComponent
    private set

internal object AppComponentManager {

    fun init(application: QKApplication) {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(application))
                .build()
    }

}