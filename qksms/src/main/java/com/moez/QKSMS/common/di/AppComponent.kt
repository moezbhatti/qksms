package com.moez.QKSMS.common.di

import com.moez.QKSMS.common.util.NotificationManager
import com.moez.QKSMS.presentation.conversations.ConversationAdapter
import com.moez.QKSMS.presentation.conversations.ConversationListViewModel
import com.moez.QKSMS.presentation.messages.MessageAdapter
import com.moez.QKSMS.presentation.messages.MessageListActivity
import com.moez.QKSMS.presentation.messages.MessageListViewModel
import com.moez.QKSMS.presentation.view.AvatarView
import com.moez.QKSMS.receiver.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(activity: MessageListActivity)

    fun inject(adapter: MessageAdapter)
    fun inject(adapter: ConversationAdapter)

    fun inject(manager: NotificationManager)

    fun inject(receiver: SmsReceiver)
    fun inject(receiver: MessageDeliveredReceiver)
    fun inject(receiver: MessageSentReceiver)
    fun inject(receiver: MarkSeenReceiver)
    fun inject(receiver: MarkReadReceiver)
    fun inject(receiver: RemoteMessagingReceiver)

    fun inject(view: AvatarView)

    fun inject(viewModel: ConversationListViewModel)
    fun inject(viewModel: MessageListViewModel)

}