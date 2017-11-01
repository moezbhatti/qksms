package com.moez.QKSMS.common.di

import com.moez.QKSMS.common.util.NotificationManager
import com.moez.QKSMS.presentation.conversations.ConversationsActivity
import com.moez.QKSMS.presentation.conversations.ConversationsAdapter
import com.moez.QKSMS.presentation.conversations.ConversationsViewModel
import com.moez.QKSMS.presentation.messages.MessagesActivity
import com.moez.QKSMS.presentation.messages.MessagesAdapter
import com.moez.QKSMS.presentation.messages.MessagesViewModel
import com.moez.QKSMS.presentation.settings.SettingsViewModel
import com.moez.QKSMS.presentation.view.AvatarView
import com.moez.QKSMS.receiver.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(activity: ConversationsActivity)
    fun inject(activity: MessagesActivity)

    fun inject(adapter: ConversationsAdapter)
    fun inject(adapter: MessagesAdapter)

    fun inject(manager: NotificationManager)

    fun inject(receiver: SmsReceiver)
    fun inject(receiver: MessageDeliveredReceiver)
    fun inject(receiver: MessageSentReceiver)
    fun inject(receiver: MarkSeenReceiver)
    fun inject(receiver: MarkReadReceiver)
    fun inject(receiver: RemoteMessagingReceiver)

    fun inject(view: AvatarView)

    fun inject(viewModel: ConversationsViewModel)
    fun inject(viewModel: MessagesViewModel)
    fun inject(viewModel: SettingsViewModel)

}