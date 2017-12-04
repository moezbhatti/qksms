package com.moez.QKSMS.presentation

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import com.moez.QKSMS.presentation.compose.ComposeActivity
import com.moez.QKSMS.presentation.compose.ComposeViewModel
import com.moez.QKSMS.presentation.main.MainViewModel
import com.moez.QKSMS.presentation.settings.SettingsActivity
import com.moez.QKSMS.presentation.settings.SettingsViewModel
import javax.inject.Inject
import javax.inject.Singleton





@Singleton
class Navigator @Inject constructor(val context: Context) {

    private fun startActivity(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun showCompose() {
        val intent = Intent(context, ComposeActivity::class.java)
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

    class ViewModelFactory(private val intent: Intent) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when (modelClass) {

                MainViewModel::class.java -> {
                    MainViewModel()
                }

                ComposeViewModel::class.java -> {
                    val threadId = intent.getLongExtra("threadId", 0)
                    ComposeViewModel(threadId)
                }

                SettingsViewModel::class.java -> {
                    SettingsViewModel()
                }

                else -> throw IllegalArgumentException("Invalid ViewModel class. If this is a new ViewModel, please add it to Navigator.kt")
            } as T
        }
    }

}