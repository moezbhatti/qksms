package com.moez.QKSMS.presentation

import android.annotation.TargetApi
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.moez.QKSMS.common.util.NotificationManager
import com.moez.QKSMS.presentation.compose.ComposeActivity
import com.moez.QKSMS.presentation.compose.ComposeViewModel
import com.moez.QKSMS.presentation.main.MainViewModel
import com.moez.QKSMS.presentation.settings.SettingsActivity
import com.moez.QKSMS.presentation.settings.SettingsViewModel
import com.moez.QKSMS.presentation.setup.SetupActivity
import com.moez.QKSMS.presentation.setup.SetupViewModel
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Navigator @Inject constructor(val context: Context) {

    private fun startActivity(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun showSetupActivity() {
        val intent = Intent(context, SetupActivity::class.java)
        startActivity(intent)
    }

    fun showCompose(body: String? = null) {
        val intent = Intent(context, ComposeActivity::class.java)
        intent.putExtra("body", body)
        startActivity(intent)
    }

    fun showConversation(threadId: Long) {
        val intent = Intent(context, ComposeActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }

    fun showSettings() {
        val intent = Intent(context, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun makePhoneCall(address: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$address"))
        startActivity(intent)
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun showNotificationSettings() {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationManager.DEFAULT_CHANNEL_ID)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        startActivity(intent)
    }

    class ViewModelFactory(private val intent: Intent) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when (modelClass) {

                MainViewModel::class.java -> {
                    MainViewModel()
                }

                SetupViewModel::class.java -> {
                    SetupViewModel()
                }

                ComposeViewModel::class.java -> {
                    val threadId = intent.getLongExtra("threadId", 0)
                    val body = intent.getStringExtra("body") ?: ""
                    ComposeViewModel(threadId, body)
                }

                SettingsViewModel::class.java -> {
                    SettingsViewModel()
                }

                else -> throw IllegalArgumentException("Invalid ViewModel class. If this is a new ViewModel, please add it to Navigator.kt")
            } as T
        }
    }

}