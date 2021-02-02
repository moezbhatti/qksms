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
package com.moez.QKSMS.injection

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModelProvider
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.moez.QKSMS.blocking.BlockingClient
import com.moez.QKSMS.blocking.BlockingManager
import com.moez.QKSMS.common.ViewModelFactory
import com.moez.QKSMS.common.util.BillingManagerImpl
import com.moez.QKSMS.common.util.NotificationManagerImpl
import com.moez.QKSMS.common.util.ShortcutManagerImpl
import com.moez.QKSMS.feature.conversationinfo.injection.ConversationInfoComponent
import com.moez.QKSMS.feature.themepicker.injection.ThemePickerComponent
import com.moez.QKSMS.listener.ContactAddedListener
import com.moez.QKSMS.listener.ContactAddedListenerImpl
import com.moez.QKSMS.manager.ActiveConversationManager
import com.moez.QKSMS.manager.ActiveConversationManagerImpl
import com.moez.QKSMS.manager.AlarmManager
import com.moez.QKSMS.manager.AlarmManagerImpl
import com.moez.QKSMS.manager.AnalyticsManager
import com.moez.QKSMS.manager.AnalyticsManagerImpl
import com.moez.QKSMS.manager.BillingManager
import com.moez.QKSMS.manager.ChangelogManager
import com.moez.QKSMS.manager.ChangelogManagerImpl
import com.moez.QKSMS.manager.KeyManager
import com.moez.QKSMS.manager.KeyManagerImpl
import com.moez.QKSMS.manager.NotificationManager
import com.moez.QKSMS.manager.PermissionManager
import com.moez.QKSMS.manager.PermissionManagerImpl
import com.moez.QKSMS.manager.RatingManager
import com.moez.QKSMS.manager.ReferralManager
import com.moez.QKSMS.manager.ReferralManagerImpl
import com.moez.QKSMS.manager.ShortcutManager
import com.moez.QKSMS.manager.WidgetManager
import com.moez.QKSMS.manager.WidgetManagerImpl
import com.moez.QKSMS.mapper.CursorToContact
import com.moez.QKSMS.mapper.CursorToContactGroup
import com.moez.QKSMS.mapper.CursorToContactGroupImpl
import com.moez.QKSMS.mapper.CursorToContactGroupMember
import com.moez.QKSMS.mapper.CursorToContactGroupMemberImpl
import com.moez.QKSMS.mapper.CursorToContactImpl
import com.moez.QKSMS.mapper.CursorToConversation
import com.moez.QKSMS.mapper.CursorToConversationImpl
import com.moez.QKSMS.mapper.CursorToMessage
import com.moez.QKSMS.mapper.CursorToMessageImpl
import com.moez.QKSMS.mapper.CursorToPart
import com.moez.QKSMS.mapper.CursorToPartImpl
import com.moez.QKSMS.mapper.CursorToRecipient
import com.moez.QKSMS.mapper.CursorToRecipientImpl
import com.moez.QKSMS.mapper.RatingManagerImpl
import com.moez.QKSMS.repository.BackupRepository
import com.moez.QKSMS.repository.BackupRepositoryImpl
import com.moez.QKSMS.repository.BlockingRepository
import com.moez.QKSMS.repository.BlockingRepositoryImpl
import com.moez.QKSMS.repository.ContactRepository
import com.moez.QKSMS.repository.ContactRepositoryImpl
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.ConversationRepositoryImpl
import com.moez.QKSMS.repository.MessageRepository
import com.moez.QKSMS.repository.MessageRepositoryImpl
import com.moez.QKSMS.repository.ScheduledMessageRepository
import com.moez.QKSMS.repository.ScheduledMessageRepositoryImpl
import com.moez.QKSMS.repository.SyncRepository
import com.moez.QKSMS.repository.SyncRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(subcomponents = [
    ConversationInfoComponent::class,
    ThemePickerComponent::class])
class AppModule(private var application: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = application

    @Provides
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun provideRxPreferences(preferences: SharedPreferences): RxSharedPreferences {
        return RxSharedPreferences.create(preferences)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
    }

    @Provides
    fun provideViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory = factory

    // Listener

    @Provides
    fun provideContactAddedListener(listener: ContactAddedListenerImpl): ContactAddedListener = listener

    // Manager

    @Provides
    fun provideBillingManager(manager: BillingManagerImpl): BillingManager = manager

    @Provides
    fun provideActiveConversationManager(manager: ActiveConversationManagerImpl): ActiveConversationManager = manager

    @Provides
    fun provideAlarmManager(manager: AlarmManagerImpl): AlarmManager = manager

    @Provides
    fun provideAnalyticsManager(manager: AnalyticsManagerImpl): AnalyticsManager = manager

    @Provides
    fun blockingClient(manager: BlockingManager): BlockingClient = manager

    @Provides
    fun changelogManager(manager: ChangelogManagerImpl): ChangelogManager = manager

    @Provides
    fun provideKeyManager(manager: KeyManagerImpl): KeyManager = manager

    @Provides
    fun provideNotificationsManager(manager: NotificationManagerImpl): NotificationManager = manager

    @Provides
    fun providePermissionsManager(manager: PermissionManagerImpl): PermissionManager = manager

    @Provides
    fun provideRatingManager(manager: RatingManagerImpl): RatingManager = manager

    @Provides
    fun provideShortcutManager(manager: ShortcutManagerImpl): ShortcutManager = manager

    @Provides
    fun provideReferralManager(manager: ReferralManagerImpl): ReferralManager = manager

    @Provides
    fun provideWidgetManager(manager: WidgetManagerImpl): WidgetManager = manager

    // Mapper

    @Provides
    fun provideCursorToContact(mapper: CursorToContactImpl): CursorToContact = mapper

    @Provides
    fun provideCursorToContactGroup(mapper: CursorToContactGroupImpl): CursorToContactGroup = mapper

    @Provides
    fun provideCursorToContactGroupMember(mapper: CursorToContactGroupMemberImpl): CursorToContactGroupMember = mapper

    @Provides
    fun provideCursorToConversation(mapper: CursorToConversationImpl): CursorToConversation = mapper

    @Provides
    fun provideCursorToMessage(mapper: CursorToMessageImpl): CursorToMessage = mapper

    @Provides
    fun provideCursorToPart(mapper: CursorToPartImpl): CursorToPart = mapper

    @Provides
    fun provideCursorToRecipient(mapper: CursorToRecipientImpl): CursorToRecipient = mapper

    // Repository

    @Provides
    fun provideBackupRepository(repository: BackupRepositoryImpl): BackupRepository = repository

    @Provides
    fun provideBlockingRepository(repository: BlockingRepositoryImpl): BlockingRepository = repository

    @Provides
    fun provideContactRepository(repository: ContactRepositoryImpl): ContactRepository = repository

    @Provides
    fun provideConversationRepository(repository: ConversationRepositoryImpl): ConversationRepository = repository

    @Provides
    fun provideMessageRepository(repository: MessageRepositoryImpl): MessageRepository = repository

    @Provides
    fun provideScheduledMessagesRepository(repository: ScheduledMessageRepositoryImpl): ScheduledMessageRepository = repository

    @Provides
    fun provideSyncRepository(repository: SyncRepositoryImpl): SyncRepository = repository

}