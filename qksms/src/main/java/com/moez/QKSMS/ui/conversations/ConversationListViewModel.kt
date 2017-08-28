package com.moez.QKSMS.ui.conversations

import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.data.repository.ConversationRepository
import com.moez.QKSMS.data.sync.SyncManager

class ConversationListViewModel(val syncManager: SyncManager, repository: ConversationRepository) : ViewModel() {

    val conversations = repository.getConversations()

    fun onRefresh(completionListener: () -> Unit) {
        syncManager.copyToRealm(completionListener::invoke)
    }

}