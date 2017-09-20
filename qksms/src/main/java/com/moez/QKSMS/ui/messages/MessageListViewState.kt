package com.moez.QKSMS.ui.messages

import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import io.realm.OrderedRealmCollection

sealed class MessageListViewState {
    data class ConversationLoaded(val conversation: Conversation) : MessageListViewState()
    data class ConversationError(val errorCode: Int) : MessageListViewState()
    data class MessagesLoaded(val messages: OrderedRealmCollection<Message>) : MessageListViewState()
    data class DraftLoaded(val draft: String) : MessageListViewState()
}