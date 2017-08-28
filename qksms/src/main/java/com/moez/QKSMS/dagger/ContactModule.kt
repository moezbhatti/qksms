package com.moez.QKSMS.dagger

import android.content.Context
import com.moez.QKSMS.data.repository.ContactRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContactModule {

    @Provides
    @Singleton
    fun provideContactRepository(context: Context): ContactRepository {
        return ContactRepository(context)
    }

}