package com.moez.QKSMS.presentation.conversations

import com.moez.QKSMS.data.model.Message
import io.realm.OrderedRealmCollection

data class ConversationListViewState(
        val conversations: OrderedRealmCollection<Message>? = null,
        val refreshing: Boolean = false
)

sealed class PartialState {

    abstract fun reduce(previousState: ConversationListViewState): ConversationListViewState

    data class ConversationsLoaded(val conversations: OrderedRealmCollection<Message>) : PartialState() {
        override fun reduce(previousState: ConversationListViewState): ConversationListViewState {
            return previousState.copy(conversations = conversations)
        }
    }

    data class Refreshing(val refreshing: Boolean) : PartialState (){
        override fun reduce(previousState: ConversationListViewState): ConversationListViewState {
            return previousState.copy(refreshing = refreshing)
        }
    }

}