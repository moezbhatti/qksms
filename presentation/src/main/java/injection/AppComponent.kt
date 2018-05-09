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
package injection

import common.QKApplication
import common.QkDialog
import common.util.ContactImageLoader
import common.widget.AvatarView
import common.widget.PagerTitleView
import common.widget.PreferenceView
import common.widget.QkEditText
import common.widget.QkSwitch
import common.widget.QkTextView
import common.widget.Separator
import dagger.Component
import feature.blocked.BlockedActivity
import feature.blocked.BlockedViewModel
import feature.compose.ComposeActivity
import feature.compose.ComposeViewModel
import feature.compose.DetailedChipView
import feature.conversationinfo.ConversationInfoActivity
import feature.conversationinfo.ConversationInfoViewModel
import feature.gallery.GalleryActivity
import feature.gallery.GalleryViewModel
import feature.main.MainActivity
import feature.main.MainViewModel
import feature.notificationprefs.NotificationPrefsActivity
import feature.notificationprefs.NotificationPrefsViewModel
import feature.plus.PlusActivity
import feature.plus.PlusViewModel
import feature.qkreply.QkReplyActivity
import feature.qkreply.QkReplyViewModel
import feature.settings.SettingsActivity
import feature.settings.SettingsViewModel
import feature.settings.about.AboutActivity
import feature.settings.about.AboutViewModel
import feature.themepicker.ThemePickerActivity
import feature.themepicker.ThemePickerViewModel
import feature.widget.WidgetAdapter
import feature.widget.WidgetProvider
import receiver.DefaultSmsChangedReceiver
import receiver.MarkReadReceiver
import receiver.MarkSeenReceiver
import receiver.MmsReceivedReceiver
import receiver.MmsSentReceiver
import receiver.MmsUpdatedReceiver
import receiver.NightModeReceiver
import receiver.RemoteMessagingReceiver
import receiver.SendSmsReceiver
import receiver.SmsDeliveredReceiver
import receiver.SmsProviderChangedReceiver
import receiver.SmsReceiver
import receiver.SmsSentReceiver
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class)])
interface AppComponent {

    fun inject(application: QKApplication)

    fun inject(activity: MainActivity)
    fun inject(activity: AboutActivity)
    fun inject(activity: BlockedActivity)
    fun inject(activity: ComposeActivity)
    fun inject(activity: ConversationInfoActivity)
    fun inject(activity: GalleryActivity)
    fun inject(activity: NotificationPrefsActivity)
    fun inject(activity: PlusActivity)
    fun inject(activity: QkReplyActivity)
    fun inject(activity: SettingsActivity)
    fun inject(activity: ThemePickerActivity)

    fun inject(dialog: QkDialog)

    fun inject(fetcher: ContactImageLoader.ContactImageFetcher)

    fun inject(receiver: DefaultSmsChangedReceiver)
    fun inject(receiver: SmsDeliveredReceiver)
    fun inject(receiver: SmsSentReceiver)
    fun inject(receiver: MarkSeenReceiver)
    fun inject(receiver: MarkReadReceiver)
    fun inject(receiver: MmsReceivedReceiver)
    fun inject(receiver: MmsSentReceiver)
    fun inject(receiver: MmsUpdatedReceiver)
    fun inject(receiver: NightModeReceiver)
    fun inject(receiver: RemoteMessagingReceiver)
    fun inject(receiver: SendSmsReceiver)
    fun inject(receiver: SmsProviderChangedReceiver)
    fun inject(receiver: SmsReceiver)
    fun inject(receiver: WidgetProvider)

    fun inject(service: WidgetAdapter)

    fun inject(view: AvatarView)
    fun inject(view: DetailedChipView)
    fun inject(view: PagerTitleView)
    fun inject(view: PreferenceView)
    fun inject(view: QkEditText)
    fun inject(view: QkSwitch)
    fun inject(view: QkTextView)
    fun inject(view: Separator)

    fun inject(viewModel: MainViewModel)
    fun inject(viewModel: AboutViewModel)
    fun inject(viewModel: BlockedViewModel)
    fun inject(viewModel: ComposeViewModel)
    fun inject(viewModel: ConversationInfoViewModel)
    fun inject(viewModel: GalleryViewModel)
    fun inject(viewModel: NotificationPrefsViewModel)
    fun inject(viewModel: PlusViewModel)
    fun inject(viewModel: QkReplyViewModel)
    fun inject(viewModel: SettingsViewModel)
    fun inject(viewModel: ThemePickerViewModel)

}