package com.moez.QKSMS.ui.messages

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.ConversationRepository
import com.moez.QKSMS.data.repository.MessageRepository
import io.realm.RealmResults

class MessageListViewModel(val threadId: Long, conversations: ConversationRepository, repository: MessageRepository) : ViewModel() {

    val state: MutableLiveData<MessageListViewState> = MutableLiveData()

    private val conversation = conversations.getConversation(threadId)
    private val messages: RealmResults<Message> = repository.getMessages()

    init {
        state.value = MessageListViewState.MessagesLoaded(messages)

        conversation.addChangeListener { realmResults ->
            when (realmResults.size) {
                0 -> state.value = MessageListViewState.ConversationError(0)
                else -> state.value = MessageListViewState.ConversationLoaded(realmResults[0])
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        conversation.removeAllChangeListeners()
        messages.removeAllChangeListeners()
    }

}