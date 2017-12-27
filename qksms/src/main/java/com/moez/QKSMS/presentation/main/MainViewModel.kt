/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.presentation.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import android.support.v4.content.ContextCompat
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.filter.ConversationFilter
import com.moez.QKSMS.data.model.MenuItem
import com.moez.QKSMS.data.model.SyncLog
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.*
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.common.base.QkViewModel
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.realm.Realm
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel : QkViewModel<MainView, MainState>(MainState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var conversationFilter: ConversationFilter
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var markAllSeen: MarkAllSeen
    @Inject lateinit var deleteConversation: DeleteConversation
    @Inject lateinit var markArchived: MarkArchived
    @Inject lateinit var markUnarchived: MarkUnarchived
    @Inject lateinit var partialSync: PartialSync

    private val conversations by lazy { messageRepo.getConversations() }

    private val menuArchive = MenuItem(R.string.menu_archive, 0)
    private val menuUnarchive = MenuItem(R.string.menu_unarchive, 1)
    private val menuDelete = MenuItem(R.string.menu_delete, 2)

    init {
        appComponent.inject(this)

        disposables += markAllSeen
        disposables += markArchived
        disposables += markUnarchived
        disposables += deleteConversation
        disposables += partialSync

        // If it's the first sync, reflect that in the ViewState
        disposables += Realm.getDefaultInstance()
                .where(SyncLog::class.java)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
                .map { it.size == 0 }
                .doOnNext { if (it) partialSync.execute(Unit) }
                .distinctUntilChanged()
                .subscribe { syncing -> newState { it.copy(syncing = syncing) } }

        newState { it.copy(page = Inbox(data = messageRepo.getConversations())) }

        val isDefaultSms = Telephony.Sms.getDefaultSmsPackage(context) != context.packageName
        val hasSmsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val hasContactPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

        if (isDefaultSms) {
            partialSync.execute(Unit)
        }

        if (isDefaultSms || !hasSmsPermission || !hasContactPermission) {
            navigator.showSetupActivity()
        }

        markAllSeen.execute(Unit)
    }

    override fun bindView(view: MainView) {
        super.bindView(view)

        intents += view.queryChangedIntent
                .skip(1)
                .debounce(200, TimeUnit.MILLISECONDS)
                .withLatestFrom(state, { query, state ->
                    if (state.page is Inbox) {
                        val conversations = when {
                            query.isEmpty() -> conversations
                            else -> conversations.map { list -> list.filter { conversationFilter.filter(it.conversation, query) } }
                        }

                        val page = state.page.copy(showClearButton = query.isNotEmpty(), data = conversations)
                        newState { it.copy(page = page) }
                    }
                })
                .subscribe()

        intents += view.queryCancelledIntent
                .subscribe { view.clearSearch() }

        intents += view.composeIntent
                .subscribe { navigator.showCompose() }

        intents += view.drawerOpenIntent
                .subscribe { open -> newState { it.copy(drawerOpen = open) } }

        intents += view.drawerItemIntent
                .doOnNext { newState { it.copy(drawerOpen = false) } }
                .doOnNext { if (it == DrawerItem.SETTINGS) navigator.showSettings() }
                .distinctUntilChanged()
                .doOnNext {
                    when (it) {
                        DrawerItem.INBOX -> newState { it.copy(page = Inbox(data = conversations)) }
                        DrawerItem.ARCHIVED -> newState { it.copy(page = Archived(messageRepo.getConversations(true))) }
                        DrawerItem.SCHEDULED -> newState { it.copy(page = Scheduled()) }
                        DrawerItem.BLOCKED -> newState { it.copy(page = Blocked()) }
                    }
                }
                .subscribe()

        intents += view.conversationClickIntent
                .doOnNext { view.clearSearch() }
                .doOnNext { threadId -> navigator.showConversation(threadId) }
                .subscribe()

        intents += view.conversationLongClickIntent
                .withLatestFrom(state, { l, mainState ->
                    when (mainState.page) {
                        is Inbox -> {
                            val page = mainState.page.copy(menu = listOf(menuArchive, menuDelete))
                            newState { it.copy(page = page) }
                        }
                        is Archived -> {
                            val page = mainState.page.copy(menu = listOf(menuUnarchive, menuDelete))
                            newState { it.copy(page = page) }
                        }
                    }
                })
                .subscribe()

        intents += view.conversationMenuItemIntent
                .withLatestFrom(state, { actionId, mainState ->
                    when (mainState.page) {
                        is Inbox -> {
                            val page = mainState.page.copy(menu = ArrayList())
                            newState { it.copy(page = page) }
                        }
                        is Archived -> {
                            val page = mainState.page.copy(menu = ArrayList())
                            newState { it.copy(page = page) }
                        }
                    }
                    actionId
                })
                .withLatestFrom(view.conversationLongClickIntent, { actionId, threadId ->
                    when (actionId) {
                        menuArchive.actionId -> markArchived.execute(threadId)
                        menuUnarchive.actionId -> markUnarchived.execute(threadId)
                        menuDelete.actionId -> deleteConversation.execute(threadId)
                    }
                })
                .subscribe()

        val swipedConversation = view.swipeConversationIntent
                .withLatestFrom(conversations.toObservable(), { position, conversations -> conversations[position] })
                .map { pair -> pair.conversation }
                .map { conversation -> conversation.id }

        intents += swipedConversation
                .withLatestFrom(state, { threadId, state ->
                    markArchived.execute(threadId) {
                        if (state.page is Inbox) {
                            val page = state.page.copy(showArchivedSnackbar = true)
                            newState { it.copy(page = page) }
                        }
                    }
                })
                .flatMap { Observable.timer(2750, TimeUnit.MILLISECONDS) }
                .withLatestFrom(state, { threadId, state ->
                    markArchived.execute(threadId) {
                        if (state.page is Inbox) {
                            val page = state.page.copy(showArchivedSnackbar = false)
                            newState { it.copy(page = page) }
                        }
                    }
                })
                .subscribe()

        intents += view.undoSwipeConversationIntent
                .withLatestFrom(swipedConversation, { _, threadId -> threadId })
                .subscribe { threadId -> markUnarchived.execute(threadId) }
    }

}