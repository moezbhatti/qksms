package com.moez.QKSMS.dagger

import com.moez.QKSMS.data.repository.MessageRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MessagesModule {

    @Singleton
    @Provides
    fun provideRepository(): MessageRepository {
        return MessageRepository()
    }

}