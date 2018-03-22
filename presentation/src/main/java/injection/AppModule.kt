/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package injection

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import common.util.AnalyticsManagerImpl
import common.util.NotificationManagerImpl
import dagger.Module
import dagger.Provides
import manager.AnalyticsManager
import manager.KeyManager
import manager.KeyManagerImpl
import manager.NotificationManager
import manager.PermissionManager
import manager.PermissionManagerImpl
import manager.WidgetManager
import manager.WidgetManagerImpl
import mapper.CursorToContact
import mapper.CursorToContactImpl
import mapper.CursorToConversation
import mapper.CursorToConversationImpl
import mapper.CursorToMessage
import mapper.CursorToMessageImpl
import mapper.CursorToPart
import mapper.CursorToPartImpl
import mapper.CursorToRecipienImpl
import mapper.CursorToRecipient
import repository.ContactRepository
import repository.ContactRepositoryImpl
import repository.MessageRepository
import repository.MessageRepositoryImpl
import repository.SyncRepository
import repository.SyncRepositoryImpl
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
    fun provideRxPreferences(context: Context): RxSharedPreferences {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return RxSharedPreferences.create(preferences)
    }

    // Manager

    @Provides
    fun provideAnalyticsManager(manager: AnalyticsManagerImpl): AnalyticsManager = manager

    @Provides
    fun provideKeyManager(manager: KeyManagerImpl): KeyManager = manager

    @Provides
    fun provideNotificationsManager(manager: NotificationManagerImpl): NotificationManager = manager

    @Provides
    fun providePermissionsManager(manager: PermissionManagerImpl): PermissionManager = manager

    @Provides
    fun provideWidgetManager(manager: WidgetManagerImpl): WidgetManager = manager


    // Mapper

    @Provides
    fun provideCursorToContact(mapper: CursorToContactImpl): CursorToContact = mapper

    @Provides
    fun provideCursorToConversation(mapper: CursorToConversationImpl): CursorToConversation = mapper

    @Provides
    fun provideCursorToMessage(mapper: CursorToMessageImpl): CursorToMessage = mapper

    @Provides
    fun provideCursorToPart(mapper: CursorToPartImpl): CursorToPart = mapper

    @Provides
    fun provideCursorToRecipient(mapper: CursorToRecipienImpl): CursorToRecipient = mapper


    // Repository

    @Provides
    fun provideContactRepository(repository: ContactRepositoryImpl): ContactRepository = repository

    @Provides
    fun provideMessageRepository(repository: MessageRepositoryImpl): MessageRepository = repository

    @Provides
    fun provideSyncRepository(repository: SyncRepositoryImpl): SyncRepository = repository

}