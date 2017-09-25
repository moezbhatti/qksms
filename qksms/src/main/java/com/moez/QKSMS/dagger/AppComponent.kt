package com.moez.QKSMS.dagger

import com.moez.QKSMS.receiver.SmsReceiver
import com.moez.QKSMS.ui.conversations.ConversationAdapter
import com.moez.QKSMS.ui.conversations.ConversationListViewModel
import com.moez.QKSMS.ui.messages.MessageAdapter
import com.moez.QKSMS.ui.messages.MessageListViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(receiver: SmsReceiver)

    fun inject(viewModel: ConversationListViewModel)
    fun inject(viewModel: MessageListViewModel)

    fun inject(adapter: MessageAdapter)
    fun inject(adapter: ConversationAdapter)

}