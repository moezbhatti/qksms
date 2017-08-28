package com.moez.QKSMS.ui.conversations

import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.data.repository.ConversationRepository

class ConversationListViewModel(repository: ConversationRepository) : ViewModel() {

    val conversations = repository.getConversations()

}