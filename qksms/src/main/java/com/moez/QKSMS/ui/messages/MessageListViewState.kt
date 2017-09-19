package com.moez.QKSMS.ui.messages

import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import io.realm.OrderedRealmCollection

sealed class MessageListViewState {
    data class ConversationLoaded(val data: Conversation) : MessageListViewState()
    data class ConversationError(val count: Int) : MessageListViewState()
    data class MessagesLoaded(val data: OrderedRealmCollection<Message>) : MessageListViewState()
}