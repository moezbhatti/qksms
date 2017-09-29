package com.moez.QKSMS.common.di

import android.app.Application
import android.content.Context
import com.moez.QKSMS.data.datasource.MessageTransaction
import com.moez.QKSMS.data.datasource.native.NativeMessageTransaction
import com.moez.QKSMS.data.datasource.realm.RealmMessageTransaction
import dagger.Module
import dagger.Provides
import javax.inject.Named
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
    @Named("Realm")
    fun provideRealmMessageTransaction() : MessageTransaction {
        return RealmMessageTransaction()
    }

    @Provides
    @Singleton
    @Named("Native")
    fun provideNativeMessageTransaction(context: Context) : MessageTransaction {
        return NativeMessageTransaction(context)
    }

}