package com.moez.QKSMS.presentation.main

import com.moez.QKSMS.data.model.ConversationMessagePair
import io.reactivex.Flowable

data class MainState(
        val page: MainPage = Inbox(null),
        val drawerOpen: Boolean = false
)

sealed class MainPage

data class Inbox(val data: Flowable<List<ConversationMessagePair>>?) : MainPage()

data class Archived(val data: Flowable<List<ConversationMessagePair>>?) : MainPage()

data class Scheduled(val data: Any? = null) : MainPage()

data class Blocked(val data: Any? = null) : MainPage()