package com.moez.QKSMS.presentation.messages

import com.moez.QKSMS.data.model.Message
import io.realm.RealmResults

data class MessagesState(
        val title: String = "",
        val messages: RealmResults<Message>? = null,
        val draft: String = "",
        val canSend: Boolean = false,
        val hasError: Boolean = false
)