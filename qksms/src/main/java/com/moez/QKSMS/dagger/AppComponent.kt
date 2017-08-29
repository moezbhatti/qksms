package com.moez.QKSMS.dagger

import android.content.Context
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.ConversationRepository
import com.moez.QKSMS.ui.conversations.ConversationListActivity
import com.moez.QKSMS.ui.messages.MessageAdapter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, ContactModule::class, ConversationsModule::class))
interface AppComponent {

    fun inject(activity: ConversationListActivity)
    fun inject(message: Message)
    fun inject(adapter: MessageAdapter)

    fun provideContext(): Context
    fun provideConversationRepository(): ConversationRepository

}