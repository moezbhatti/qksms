package com.moez.QKSMS.common.di

import com.moez.QKSMS.common.QKApplication
import com.moez.QKSMS.common.util.ContactImageLoader
import com.moez.QKSMS.presentation.common.widget.*
import com.moez.QKSMS.presentation.compose.ComposeActivity
import com.moez.QKSMS.presentation.compose.ComposeViewModel
import com.moez.QKSMS.presentation.compose.DetailedChipView
import com.moez.QKSMS.presentation.main.MainActivity
import com.moez.QKSMS.presentation.main.MainViewModel
import com.moez.QKSMS.presentation.settings.SettingsActivity
import com.moez.QKSMS.presentation.settings.SettingsViewModel
import com.moez.QKSMS.presentation.setup.SetupActivity
import com.moez.QKSMS.presentation.setup.SetupViewModel
import com.moez.QKSMS.receiver.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class)])
interface AppComponent {

    fun inject(application: QKApplication)

    fun inject(activity: MainActivity)
    fun inject(activity: SetupActivity)
    fun inject(activity: ComposeActivity)
    fun inject(activity: SettingsActivity)

    fun inject(receiver: DefaultSmsChangedReceiver)
    fun inject(receiver: SmsReceiver)
    fun inject(receiver: MessageDeliveredReceiver)
    fun inject(receiver: MessageSentReceiver)
    fun inject(receiver: MarkSeenReceiver)
    fun inject(receiver: MarkReadReceiver)
    fun inject(receiver: RemoteMessagingReceiver)

    fun inject(view: AvatarView)
    fun inject(view: DetailedChipView)
    fun inject(view: QkEditText)
    fun inject(view: QkSwitch)
    fun inject(view: QkTextView)
    fun inject(view: Separator)

    fun inject(viewModel: MainViewModel)
    fun inject(viewModel: SetupViewModel)
    fun inject(viewModel: ComposeViewModel)
    fun inject(viewModel: SettingsViewModel)

    fun inject(fetcher: ContactImageLoader.ContactImageFetcher)

}