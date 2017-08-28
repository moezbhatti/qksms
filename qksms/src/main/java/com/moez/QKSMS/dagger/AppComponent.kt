package com.moez.QKSMS.dagger

import android.content.Context
import com.moez.QKSMS.ui.conversations.ConversationListActivity
import com.moez.QKSMS.data.repository.ConversationRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, ContactModule::class, ConversationsModule::class))
interface AppComponent {

    fun inject(activity: ConversationListActivity)

    fun provideContext(): Context
    fun provideConversationRepository(): ConversationRepository

}