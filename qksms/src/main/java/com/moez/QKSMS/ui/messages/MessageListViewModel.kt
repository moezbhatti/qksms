package com.moez.QKSMS.ui.messages

import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.ConversationRepository
import com.moez.QKSMS.data.repository.MessageRepository
import io.realm.RealmResults

class MessageListViewModel(val threadId: Long, conversations: ConversationRepository, repository: MessageRepository) : ViewModel() {

    val conversation = conversations.getConversation(threadId)
    val messages: RealmResults<Message> = repository.getMessages()

    override fun onCleared() {
        super.onCleared()
        conversation.removeAllChangeListeners()
        messages.removeAllChangeListeners()
    }

}