package com.moez.QKSMS.ui.messages

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.ConversationRepository
import com.moez.QKSMS.data.repository.MessageRepository
import io.realm.RealmResults
import javax.inject.Inject

class MessageListViewModel : ViewModel() {

    @Inject lateinit var conversationRepo: ConversationRepository
    @Inject lateinit var messageRepo: MessageRepository

    val state: MutableLiveData<MessageListViewState> = MutableLiveData()

    private var conversation: RealmResults<Conversation>? = null
    private var messages: RealmResults<Message>? = null

    init {
        AppComponentManager.appComponent.inject(this)
    }

    fun setThreadId(threadId: Long) {
        onCleared()

        messageRepo.getMessages(threadId).let {
            messages = it
            state.value = MessageListViewState.MessagesLoaded(messages!!)
        }

        conversationRepo.getConversation(threadId).let {
            conversation = it
            it.addChangeListener { realmResults ->
                when (realmResults.size) {
                    0 -> state.value = MessageListViewState.ConversationError(0)
                    else -> state.value = MessageListViewState.ConversationLoaded(realmResults[0])
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        conversation?.removeAllChangeListeners()
        messages?.removeAllChangeListeners()
    }

}