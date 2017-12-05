package com.moez.QKSMS.presentation.main

import com.moez.QKSMS.data.model.ConversationMessagePair

data class MainState(
        val page: MainPage = MainPage.INBOX,
        val conversations: List<ConversationMessagePair> = ArrayList(),
        val archivedConversations: List<ConversationMessagePair> = ArrayList(),
        val refreshing: Boolean = false,
        val drawerOpen: Boolean = false
)

enum class MainPage { INBOX, ARCHIVED, SCHEDULED, BLOCKED }