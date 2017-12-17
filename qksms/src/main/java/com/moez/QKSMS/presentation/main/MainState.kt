package com.moez.QKSMS.presentation.main

import com.moez.QKSMS.data.model.InboxItem
import com.moez.QKSMS.data.model.MenuItem
import io.reactivex.Flowable

data class MainState(
        val page: MainPage = Inbox(),
        val drawerOpen: Boolean = false
)

sealed class MainPage

data class Inbox(
        val query: CharSequence = "",
        val data: Flowable<List<InboxItem>>? = null,
        val menu: List<MenuItem> = ArrayList(),
        val showArchivedSnackbar: Boolean = false) : MainPage()

data class Archived(
        val data: Flowable<List<InboxItem>>?,
        val menu: List<MenuItem> = ArrayList()) : MainPage()

data class Scheduled(
        val data: Any? = null) : MainPage()

data class Blocked(
        val data: Any? = null) : MainPage()