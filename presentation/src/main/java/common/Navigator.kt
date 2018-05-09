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
package common

import android.app.Activity
import android.app.ActivityOptions
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.provider.Telephony
import android.view.View
import com.moez.QKSMS.BuildConfig
import feature.blocked.BlockedActivity
import feature.blocked.BlockedViewModel
import feature.compose.ComposeActivity
import feature.compose.ComposeViewModel
import feature.conversationinfo.ConversationInfoActivity
import feature.conversationinfo.ConversationInfoViewModel
import feature.gallery.GalleryActivity
import feature.gallery.GalleryViewModel
import feature.main.MainViewModel
import feature.notificationprefs.NotificationPrefsActivity
import feature.notificationprefs.NotificationPrefsViewModel
import feature.plus.PlusActivity
import feature.plus.PlusViewModel
import feature.qkreply.QkReplyViewModel
import feature.settings.SettingsActivity
import feature.settings.SettingsViewModel
import feature.settings.about.AboutActivity
import feature.settings.about.AboutViewModel
import feature.themepicker.ThemePickerActivity
import feature.themepicker.ThemePickerViewModel
import manager.NotificationManager
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

    fun showCompose(body: String? = null, images: List<Uri>? = null) {
        val intent = Intent(context, ComposeActivity::class.java)
        intent.putExtra(Intent.EXTRA_TEXT, body)

        images?.takeIf { it.isNotEmpty() }?.let {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(images))
        }

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

    fun showVideo(uri: Uri, type: String) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
                .setDataAndType(uri, type)

        startActivityExternal(intent)
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

    fun showAbout() {
        val intent = Intent(context, AboutActivity::class.java)
        startActivity(intent)
    }

    fun showDeveloper() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moezbhatti"))
        startActivity(intent)
    }

    fun showSourceCode() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moezbhatti/qksms"))
        startActivity(intent)
    }

    fun showChangelog() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moezbhatti/qksms/releases"))
        startActivity(intent)
    }

    fun showLicense() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moezbhatti/qksms/blob/master/LICENSE"))
        startActivity(intent)
    }

    fun showBlockedConversations() {
        val intent = Intent(context, BlockedActivity::class.java)
        startActivity(intent)
    }

    fun showThemePicker(threadId: Long = 0) {
        val intent = Intent(context, ThemePickerActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }

    fun makePhoneCall(address: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$address"))
        startActivityExternal(intent)
    }

    fun showDonation() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/QKSMSDonation"))
        startActivity(intent)
    }


    fun showRating() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                        or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)))
        }
    }

    /**
     * Launch the Play Store and display the Should I Answer? listing
     */
    fun showSia() {
        val url = "https://play.google.com/store/apps/details?id=org.mistergroup.shouldianswerpersonal"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    fun showSupport() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("team@qklabs.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "QKSMS Support")
        intent.putExtra(Intent.EXTRA_TEXT, StringBuilder("\n\n")
                .append("--- Please write your message above this line ---\n\n")
                .append("Version: ${BuildConfig.VERSION_NAME}\n")
                .append("Device: ${Build.BRAND} ${Build.MODEL}\n")
                .append("SDK: ${Build.VERSION.SDK_INT}")
                .toString())
        startActivityExternal(intent)
    }

    fun addContact(address: String) {
        val uri = Uri.parse("tel: $address")
        val intent = Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, uri)
        startActivityExternal(intent)
    }

    fun showNotificationSettings(threadId: Long = 0) {
        val intent = Intent(context, NotificationPrefsActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }

    fun showNotificationChannel(threadId: Long = 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (threadId != 0L) {
                notificationManager.createNotificationChannel(threadId)
            }

            val channelId = notificationManager.buildNotificationChannelId(threadId)
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            startActivity(intent)
        }
    }

    class ViewModelFactory(private val intent: Intent) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when (modelClass) {
                MainViewModel::class.java -> MainViewModel()
                PlusViewModel::class.java -> PlusViewModel()
                AboutViewModel::class.java -> AboutViewModel()
                ComposeViewModel::class.java -> ComposeViewModel(intent)
                ConversationInfoViewModel::class.java -> ConversationInfoViewModel(intent)
                GalleryViewModel::class.java -> GalleryViewModel(intent)
                NotificationPrefsViewModel::class.java -> NotificationPrefsViewModel(intent)
                QkReplyViewModel::class.java -> QkReplyViewModel(intent)
                SettingsViewModel::class.java -> SettingsViewModel()
                BlockedViewModel::class.java -> BlockedViewModel()
                ThemePickerViewModel::class.java -> ThemePickerViewModel(intent)
                else -> throw IllegalArgumentException("Invalid ViewModel class. If this is a new ViewModel, please add it to Navigator.kt")
            } as T
        }
    }

}