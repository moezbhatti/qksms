package com.moez.QKSMS.ui.conversations

import com.moez.QKSMS.data.model.Conversation
import io.realm.OrderedRealmCollection

data class ConversationListViewState(
        val conversations: OrderedRealmCollection<Conversation>? = null,
        val refreshing: Boolean = false
)

sealed class PartialState {

    abstract fun reduce(previousState: ConversationListViewState): ConversationListViewState

    data class ConversationsLoaded(val conversations: OrderedRealmCollection<Conversation>) : PartialState() {
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