package com.moez.QKSMS.presentation.conversations

import com.moez.QKSMS.data.model.Message
import io.realm.OrderedRealmCollection

data class ConversationsState(
        val conversations: OrderedRealmCollection<Message>? = null,
        val refreshing: Boolean = false,
        val drawerOpen: Boolean = false
)