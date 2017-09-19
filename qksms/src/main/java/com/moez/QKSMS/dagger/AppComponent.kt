package com.moez.QKSMS.dagger

import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.receiver.SmsReceiver
import com.moez.QKSMS.ui.conversations.ConversationListViewModel
import com.moez.QKSMS.ui.messages.MessageAdapter
import com.moez.QKSMS.ui.messages.MessageListViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, ContactModule::class, ConversationsModule::class, MessagesModule::class))
interface AppComponent {

    fun inject(receiver: SmsReceiver)

    fun inject(viewModel: ConversationListViewModel)
    fun inject(viewModel: MessageListViewModel)

    fun inject(message: Message)
    fun inject(adapter: MessageAdapter)

}