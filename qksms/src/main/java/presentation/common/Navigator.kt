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
package presentation.common

import android.app.Activity
import android.app.ActivityOptions
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.provider.Telephony
import android.view.View
import common.util.NotificationManager
import presentation.feature.blocked.BlockedActivity
import presentation.feature.blocked.BlockedViewModel
import presentation.feature.compose.ComposeActivity
import presentation.feature.compose.ComposeViewModel
import presentation.feature.conversationinfo.ConversationInfoActivity
import presentation.feature.conversationinfo.ConversationInfoViewModel
import presentation.feature.gallery.GalleryActivity
import presentation.feature.gallery.GalleryViewModel
import presentation.feature.main.MainViewModel
import presentation.feature.notificationprefs.NotificationPrefsActivity
import presentation.feature.notificationprefs.NotificationPrefsViewModel
import presentation.feature.plus.PlusActivity
import presentation.feature.plus.PlusViewModel
import presentation.feature.settings.SettingsActivity
import presentation.feature.settings.SettingsViewModel
import presentation.feature.setup.SetupActivity
import presentation.feature.setup.SetupViewModel
import presentation.feature.themepicker.ThemePickerActivity
import presentation.feature.themepicker.ThemePickerViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor(private val context: Context, private val notificationManager: NotificationManager) {

    private fun startActivity(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun startActivityExternal(intent: Intent) {
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent.createChooser(intent, null))
        }
    }

    fun showSetupActivity() {
        val intent = Intent(context, SetupActivity::class.java)
        startActivity(intent)
    }

    fun showQksmsPlusActivity() {
        val intent = Intent(context, PlusActivity::class.java)
        startActivity(intent)
    }

    fun showDefaultSmsDialog() {
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
        }
        startActivity(intent)
    }

    fun showCompose(body: String? = null) {
        val intent = Intent(context, ComposeActivity::class.java)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        startActivity(intent)
    }

    fun showConversation(threadId: Long) {
        val intent = Intent(context, ComposeActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }

    fun showConversationInfo(threadId: Long) {
        val intent = Intent(context, ConversationInfoActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }

    fun showImage(partId: Long) {
        val intent = Intent(context, GalleryActivity::class.java)
        intent.putExtra("partId", partId)
        startActivity(intent)
    }

    /**
     * Shows the attachment full-screen
     * The transitionName for the view should be the id of the image being displayed
     */
    fun showImageAnimated(from: Activity, view: View) {
        val intent = Intent(context, GalleryActivity::class.java)
        intent.putExtra("partId", view.transitionName.toLong())

        val options = ActivityOptions.makeSceneTransitionAnimation(from, view, view.transitionName)
        from.startActivity(intent, options.toBundle())
    }

    fun showSettings() {
        val intent = Intent(context, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun showBlockedConversations() {
        val intent = Intent(context, BlockedActivity::class.java)
        startActivity(intent)
    }

    fun showThemePicker() {
        val intent = Intent(context, ThemePickerActivity::class.java)
        startActivity(intent)
    }

    fun makePhoneCall(address: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$address"))
        startActivityExternal(intent)
    }

    fun showSupport() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("team@qklabs.com"))
        startActivityExternal(intent)
    }

    fun addContact(address: String) {
        val uri = Uri.parse("tel: $address")
        val intent = Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, uri)
        startActivityExternal(intent)
    }

    fun showNotificationSettings(threadId: Long = 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (threadId != 0L) {
                notificationManager.createNotificationChannel(threadId)
            }

            val channelId = notificationManager.buildNotificationChannelId(threadId)
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            startActivity(intent)
        } else {
            val intent = Intent(context, NotificationPrefsActivity::class.java)
            intent.putExtra("threadId", threadId)
            startActivity(intent)
        }
    }

    class ViewModelFactory(private val intent: Intent) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when (modelClass) {
                MainViewModel::class.java -> MainViewModel()
                PlusViewModel::class.java -> PlusViewModel()
                SetupViewModel::class.java -> SetupViewModel()
                ComposeViewModel::class.java -> ComposeViewModel(intent)
                ConversationInfoViewModel::class.java -> ConversationInfoViewModel(intent)
                GalleryViewModel::class.java -> GalleryViewModel(intent)
                NotificationPrefsViewModel::class.java -> NotificationPrefsViewModel(intent)
                SettingsViewModel::class.java -> SettingsViewModel()
                BlockedViewModel::class.java -> BlockedViewModel()
                ThemePickerViewModel::class.java -> ThemePickerViewModel()
                else -> throw IllegalArgumentException("Invalid ViewModel class. If this is a new ViewModel, please add it to Navigator.kt")
            } as T
        }
    }

}