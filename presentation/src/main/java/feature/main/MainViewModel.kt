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
package feature.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import android.support.v4.content.ContextCompat
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.Navigator
import common.base.QkViewModel
import common.util.filter.ConversationFilter
import injection.appComponent
import interactor.DeleteConversations
import interactor.MarkAllSeen
import interactor.MarkArchived
import interactor.MarkBlocked
import interactor.MarkUnarchived
import interactor.MigratePreferences
import interactor.PartialSync
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.realm.Realm
import manager.RatingManager
import model.SyncLog
import repository.MessageRepository
import util.Preferences
import util.extensions.removeAccents
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel : QkViewModel<MainView, MainState>(MainState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var conversationFilter: ConversationFilter
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var markAllSeen: MarkAllSeen
    @Inject lateinit var deleteConversations: DeleteConversations
    @Inject lateinit var markArchived: MarkArchived
    @Inject lateinit var markUnarchived: MarkUnarchived
    @Inject lateinit var markBlocked: MarkBlocked
    @Inject lateinit var migratePreferences: MigratePreferences
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var partialSync: PartialSync
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var ratingManager: RatingManager

    private val conversations by lazy { messageRepo.getConversations() }

    init {
        appComponent.inject(this)

        // Now the conversations can be loaded
        conversations

        disposables += deleteConversations
        disposables += markAllSeen
        disposables += markArchived
        disposables += markUnarchived
        disposables += migratePreferences
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

        disposables += ratingManager.shouldShowRating
                .subscribe { show -> newState { it.copy(showRating = show) } }

        // Migrate the preferences from 2.7.3 if necessary
        migratePreferences.execute(Unit)

        val isNotDefaultSms = Telephony.Sms.getDefaultSmsPackage(context) != context.packageName
        val hasSmsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val hasContactPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

        if (isNotDefaultSms) {
            partialSync.execute(Unit)
        }

        if (isNotDefaultSms || !hasSmsPermission || !hasContactPermission) {
            navigator.showSetupActivity()
        }

        ratingManager.addSession()
        markAllSeen.execute(Unit)
    }

    override fun bindView(view: MainView) {
        super.bindView(view)

        view.queryChangedIntent
                .debounce(200, TimeUnit.MILLISECONDS)
                .map { query -> query.removeAccents() }
                .withLatestFrom(state, { query, state ->
                    if (state.page is Inbox) {
                        val filteredConversations = if (query.isEmpty()) conversations
                        else conversations
                                .map { conversations -> conversations.filter { conversationFilter.filter(it, query) } }

                        val page = state.page.copy(showClearButton = query.isNotEmpty(), data = filteredConversations)
                        newState { it.copy(page = page) }
                    }
                })
                .autoDisposable(view.scope())
                .subscribe()

        view.composeIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showCompose() }

        view.homeIntent
                .withLatestFrom(state, { _, state ->
                    when {
                        state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                        state.page is Inbox && state.page.showClearButton -> view.clearSearch()

                        state.page is Archived && state.page.selected > 0 -> view.clearSelection()

                        else -> newState { it.copy(drawerOpen = true) }
                    }
                })
                .autoDisposable(view.scope())
                .subscribe()

        view.drawerOpenIntent
                .autoDisposable(view.scope())
                .subscribe { open -> newState { it.copy(drawerOpen = open) } }

        view.drawerItemIntent
                .doOnNext { newState { it.copy(drawerOpen = false) } }
                .doOnNext { if (it == DrawerItem.SETTINGS) navigator.showSettings() }
                .doOnNext { if (it == DrawerItem.PLUS) navigator.showQksmsPlusActivity() }
                .doOnNext { if (it == DrawerItem.HELP) navigator.showSupport() }
                .distinctUntilChanged()
                .doOnNext {
                    when (it) {
                        DrawerItem.INBOX -> newState { it.copy(page = Inbox(data = conversations)) }
                        DrawerItem.ARCHIVED -> newState { it.copy(page = Archived(data = messageRepo.getConversations(true))) }
                        DrawerItem.SCHEDULED -> newState { it.copy(page = Scheduled()) }
                        else -> {
                        } // Do nothing
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .withLatestFrom(view.conversationsSelectedIntent, { itemId, conversations ->
                    when (itemId) {
                        R.id.archive -> {
                            markArchived.execute(conversations)
                            view.clearSelection()
                        }

                        R.id.unarchive -> {
                            markUnarchived.execute(conversations)
                            view.clearSelection()
                        }

                        R.id.block -> {
                            markBlocked.execute(conversations)
                            view.clearSelection()
                        }

                        R.id.delete -> view.showDeleteDialog()
                    }
                })
                .autoDisposable(view.scope())
                .subscribe()

        view.rateIntent
                .autoDisposable(view.scope())
                .subscribe {
                    navigator.showRating()
                    ratingManager.rate()
                }

        view.dismissRatingIntent
                .autoDisposable(view.scope())
                .subscribe { ratingManager.dismiss() }

        view.conversationsSelectedIntent
                .map { selection -> selection.size }
                .withLatestFrom(state, { selected, state ->
                    when (state.page) {
                        is Inbox -> {
                            val page = state.page.copy(selected = selected, showClearButton = selected > 0)
                            newState { it.copy(page = page) }
                        }

                        is Archived -> {
                            val page = state.page.copy(selected = selected, showClearButton = selected > 0)
                            newState { it.copy(page = page) }
                        }
                    }
                })
                .autoDisposable(view.scope())
                .subscribe()

        // Delete the conversation
        view.confirmDeleteIntent
                .withLatestFrom(view.conversationsSelectedIntent, { _, conversations -> conversations })
                .autoDisposable(view.scope())
                .subscribe { conversations ->
                    deleteConversations.execute(conversations)
                    view.clearSelection()
                }

        view.swipeConversationIntent
                .withLatestFrom(state, { threadId, state ->
                    markArchived.execute(listOf(threadId)) {
                        if (state.page is Inbox) {
                            val page = state.page.copy(showArchivedSnackbar = true)
                            newState { it.copy(page = page) }
                        }
                    }
                })
                .switchMap { Observable.timer(2750, TimeUnit.MILLISECONDS) }
                .withLatestFrom(state, { threadId, state ->
                    markArchived.execute(listOf(threadId)) {
                        if (state.page is Inbox) {
                            val page = state.page.copy(showArchivedSnackbar = false)
                            newState { it.copy(page = page) }
                        }
                    }
                })
                .autoDisposable(view.scope())
                .subscribe()

        view.undoSwipeConversationIntent
                .withLatestFrom(view.swipeConversationIntent, { _, threadId -> threadId })
                .autoDisposable(view.scope())
                .subscribe { threadId -> markUnarchived.execute(listOf(threadId)) }

        view.backPressedIntent
                .withLatestFrom(state, { _, state ->
                    when {
                        state.drawerOpen -> newState { it.copy(drawerOpen = false) }

                        state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                        state.page is Inbox && state.page.showClearButton -> view.clearSearch()

                        state.page is Archived && state.page.selected > 0 -> view.clearSelection()

                        state.page !is Inbox -> newState { it.copy(page = Inbox(data = conversations)) }

                        else -> newState { it.copy(hasError = true) }
                    }
                })
                .autoDisposable(view.scope())
                .subscribe()
    }

}