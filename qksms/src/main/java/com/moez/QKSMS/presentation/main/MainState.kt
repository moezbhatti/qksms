package com.moez.QKSMS.presentation.main

import com.moez.QKSMS.data.model.InboxItem
import io.reactivex.Flowable

data class MainState(
        val page: MainPage = Inbox(null),
        val drawerOpen: Boolean = false
)

sealed class MainPage

data class Inbox(val data: Flowable<List<InboxItem>>?) : MainPage()

data class Archived(val data: Flowable<List<InboxItem>>?) : MainPage()

data class Scheduled(val data: Any? = null) : MainPage()

data class Blocked(val data: Any? = null) : MainPage()