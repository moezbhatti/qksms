/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package common.di

import common.QKApplication
import common.util.ContactImageLoader
import dagger.Component
import presentation.common.widget.*
import presentation.feature.compose.ComposeActivity
import presentation.feature.compose.ComposeViewModel
import presentation.feature.compose.DetailedChipView
import presentation.feature.main.MainActivity
import presentation.feature.main.MainViewModel
import presentation.feature.plus.PlusActivity
import presentation.feature.plus.PlusViewModel
import presentation.feature.settings.SettingsActivity
import presentation.feature.settings.SettingsViewModel
import presentation.feature.setup.SetupActivity
import presentation.feature.setup.SetupViewModel
import presentation.feature.themepicker.ThemePickerActivity
import presentation.feature.themepicker.ThemePickerViewModel
import presentation.receiver.*
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class)])
interface AppComponent {

    fun inject(application: QKApplication)

    fun inject(activity: MainActivity)
    fun inject(activity: SetupActivity)
    fun inject(activity: ComposeActivity)
    fun inject(activity: PlusActivity)
    fun inject(activity: SettingsActivity)
    fun inject(activity: ThemePickerActivity)

    fun inject(receiver: DefaultSmsChangedReceiver)
    fun inject(receiver: SmsReceiver)
    fun inject(receiver: MessageDeliveredReceiver)
    fun inject(receiver: MessageSentReceiver)
    fun inject(receiver: MarkSeenReceiver)
    fun inject(receiver: MarkReadReceiver)
    fun inject(receiver: MmsReceivedReceiver)
    fun inject(receiver: MmsSentReceiver)
    fun inject(receiver: MmsUpdatedReceiver)
    fun inject(receiver: RemoteMessagingReceiver)

    fun inject(view: AvatarView)
    fun inject(view: DetailedChipView)
    fun inject(view: PreferenceView)
    fun inject(view: QkEditText)
    fun inject(view: QkSwitch)
    fun inject(view: QkTextView)
    fun inject(view: Separator)

    fun inject(viewModel: MainViewModel)
    fun inject(viewModel: SetupViewModel)
    fun inject(viewModel: ComposeViewModel)
    fun inject(viewModel: PlusViewModel)
    fun inject(viewModel: SettingsViewModel)
    fun inject(viewModel: ThemePickerViewModel)

    fun inject(fetcher: ContactImageLoader.ContactImageFetcher)

}