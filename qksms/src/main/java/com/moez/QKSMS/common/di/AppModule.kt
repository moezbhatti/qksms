package com.moez.QKSMS.common.di

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private var application: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideRxPreferences(context: Context) : RxSharedPreferences {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return RxSharedPreferences.create(preferences)
    }

}