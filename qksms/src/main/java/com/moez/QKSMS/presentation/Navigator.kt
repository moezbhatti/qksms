package com.moez.QKSMS.presentation

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import com.moez.QKSMS.presentation.messages.MessagesActivity
import com.moez.QKSMS.presentation.messages.MessagesViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor(val context: Context) {

    private fun startActivity(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun showConversation(threadId: Long) {
        val intent = Intent(context, MessagesActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }

    class ViewModelFactory(private val intent: Intent) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when (modelClass) {
                MessagesViewModel::class.java -> {
                    MessagesViewModel(intent.getLongExtra("threadId", 0))
                }

                else -> throw IllegalArgumentException("Invalid ViewModel class. If this is a new ViewModel, please add it to Navigator.kt")
            } as T
        }
    }

}