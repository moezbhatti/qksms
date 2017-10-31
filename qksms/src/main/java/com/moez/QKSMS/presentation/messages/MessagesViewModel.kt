package com.moez.QKSMS.presentation.messages

import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.MarkRead
import com.moez.QKSMS.domain.interactor.SendMessage
import com.moez.QKSMS.presentation.base.QkViewModel
import javax.inject.Inject

class MessagesViewModel(val threadId: Long) : QkViewModel<MessagesView, MessagesState>(MessagesState()) {

    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var sendMessage: SendMessage
    @Inject lateinit var markRead: MarkRead

    private var conversation: Conversation

    init {
        AppComponentManager.appComponent.inject(this)

        dataChanged()
        conversation = messageRepo.getConversationAsync(threadId)
        conversation.addChangeListener { conversation: Conversation ->
            when (conversation.isValid) {
                true -> {
                    val title = conversation.getTitle()
                    val messages = messageRepo.getMessages(threadId)
                    newState { it.copy(title = title, messages = messages) }
                }
                false -> newState { it.copy(hasError = true) }
            }
        }
    }

    override fun bindIntents(view: MessagesView) {
        super.bindIntents(view)

        view.textChangedIntent.subscribe { text ->
            newState { it.copy(draft = text.toString(), canSend = text.isNotEmpty()) }
        }

        view.sendIntent.subscribe {
            val previousState = state.value!!
            sendMessage.execute(SendMessage.Params(threadId, conversation.contacts[0].address, previousState.draft))
            newState { it.copy(draft = "", canSend = false) }
        }
    }

    fun dataChanged() {
        markRead.execute(threadId)
    }

    override fun onCleared() {
        super.onCleared()
        sendMessage.dispose()
        markRead.dispose()
        conversation.removeAllChangeListeners()
    }

}