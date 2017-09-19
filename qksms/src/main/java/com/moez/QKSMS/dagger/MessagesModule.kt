package com.moez.QKSMS.dagger

import android.content.Context
import com.moez.QKSMS.data.repository.MessageRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MessagesModule {

    @Singleton
    @Provides
    fun provideRepository(context: Context): MessageRepository {
        return MessageRepository(context)
    }

}