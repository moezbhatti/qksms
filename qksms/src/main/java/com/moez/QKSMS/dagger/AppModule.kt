package com.moez.QKSMS.dagger

import android.app.Application
import android.content.Context
import com.moez.QKSMS.data.sync.SyncManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(var application: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideSyncManager(context: Context): SyncManager {
        return SyncManager(context)
    }

}