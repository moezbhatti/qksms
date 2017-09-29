package com.moez.QKSMS.common.di

import android.app.Application
import android.content.Context
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.NotificationManager
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.data.repository.ContactRepository
import com.moez.QKSMS.data.repository.MessageRepository
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
    fun provideDateFormatter(context: Context): DateFormatter {
        return DateFormatter(context)
    }

    @Provides
    @Singleton
    fun provideSyncManager(context: Context, contacts: ContactRepository): SyncManager {
        return SyncManager(context, contacts)
    }

    @Provides
    @Singleton
    fun provideThemeMaager(): ThemeManager {
        return ThemeManager()
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(context: Context, themeManager: ThemeManager): NotificationManager {
        return NotificationManager(context, themeManager)
    }

    @Singleton
    @Provides
    fun provideMessageRepository(context: Context, notificationManager: NotificationManager): MessageRepository {
        return MessageRepository(context, notificationManager)
    }

    @Provides
    @Singleton
    fun provideContactRepository(context: Context): ContactRepository {
        return ContactRepository(context)
    }

}