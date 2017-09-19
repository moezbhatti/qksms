package com.moez.QKSMS.ui.conversations

import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.ConversationRepository
import com.moez.QKSMS.data.sync.SyncManager
import io.realm.RealmResults
import javax.inject.Inject

class ConversationListViewModel : ViewModel() {

    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var conversationRepo: ConversationRepository

    val conversations: RealmResults<Conversation>

    init {
        AppComponentManager.appComponent.inject(this)

        conversations = conversationRepo.getConversations()
    }

    fun onRefresh(completionListener: () -> Unit) {
        syncManager.copyToRealm(completionListener::invoke)
    }

    override fun onCleared() {
        super.onCleared()
        conversations.removeAllChangeListeners()
    }

}