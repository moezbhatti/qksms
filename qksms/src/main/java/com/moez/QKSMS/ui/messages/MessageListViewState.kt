package com.moez.QKSMS.ui.messages

import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import io.realm.OrderedRealmCollection

data class MessageListViewState(
        val title: String = "",
        val messages: OrderedRealmCollection<Message>? = null,
        val draft: String = "",
        val hasError: Boolean = false
) {
    val canSend: Boolean get() = draft.isNotBlank()
}

sealed class PartialState {

    abstract fun reduce(previousState: MessageListViewState): MessageListViewState

    data class ConversationLoaded(private val conversation: Conversation) : PartialState() {
        override fun reduce(previousState: MessageListViewState): MessageListViewState {
            return previousState.copy(title = conversation.getTitle())
        }
    }

    data class ConversationError(private val hasError: Boolean) : PartialState() {
        override fun reduce(previousState: MessageListViewState): MessageListViewState {
            return previousState.copy(hasError = hasError)
        }
    }

    data class MessagesLoaded(private val messages: OrderedRealmCollection<Message>) : PartialState() {
        override fun reduce(previousState: MessageListViewState): MessageListViewState {
            return previousState.copy(messages = messages)
        }
    }

    data class TextChanged(private val text: String) : PartialState() {
        override fun reduce(previousState: MessageListViewState): MessageListViewState {
            return previousState.copy(draft = text)
        }
    }

}