package com.moez.QKSMS.dagger

import com.moez.QKSMS.ui.conversations.ConversationListFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, ConversationModule::class))
interface AppComponent {

    fun inject(fragment: ConversationListFragment)

}