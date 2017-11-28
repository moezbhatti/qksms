package com.moez.QKSMS.common.di

import com.moez.QKSMS.common.util.ContactImageLoader
import com.moez.QKSMS.presentation.compose.ComposeViewModel
import com.moez.QKSMS.presentation.compose.DetailedChipView
import com.moez.QKSMS.presentation.main.ConversationsAdapter
import com.moez.QKSMS.presentation.main.MainActivity
import com.moez.QKSMS.presentation.main.MainViewModel
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

    fun inject(activity: MainActivity)
    fun inject(activity: MessagesActivity)

    fun inject(adapter: ConversationsAdapter)
    fun inject(adapter: MessagesAdapter)

    fun inject(receiver: DefaultSmsChangedReceiver)
    fun inject(receiver: SmsReceiver)
    fun inject(receiver: MessageDeliveredReceiver)
    fun inject(receiver: MessageSentReceiver)
    fun inject(receiver: MarkSeenReceiver)
    fun inject(receiver: MarkReadReceiver)
    fun inject(receiver: RemoteMessagingReceiver)

    fun inject(view: AvatarView)

    fun inject(viewModel: MainViewModel)
    fun inject(viewModel: ComposeViewModel)
    fun inject(viewModel: MessagesViewModel)
    fun inject(viewModel: SettingsViewModel)

    fun inject(fetcher: ContactImageLoader.ContactImageFetcher)

    fun inject(view: DetailedChipView)

}