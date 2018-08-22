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
package com.moez.QKSMS.feature.main

import androidx.recyclerview.widget.ItemTouchHelper
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkViewModel
import com.moez.QKSMS.common.util.BillingManager
import com.moez.QKSMS.extensions.removeAccents
import com.moez.QKSMS.interactor.DeleteConversations
import com.moez.QKSMS.interactor.MarkAllSeen
import com.moez.QKSMS.interactor.MarkArchived
import com.moez.QKSMS.interactor.MarkBlocked
import com.moez.QKSMS.interactor.MarkPinned
import com.moez.QKSMS.interactor.MarkRead
import com.moez.QKSMS.interactor.MarkUnarchived
import com.moez.QKSMS.interactor.MarkUnpinned
import com.moez.QKSMS.interactor.MarkUnread
import com.moez.QKSMS.interactor.MigratePreferences
import com.moez.QKSMS.interactor.SyncMessages
import com.moez.QKSMS.manager.PermissionManager
import com.moez.QKSMS.manager.RatingManager
import com.moez.QKSMS.model.SyncLog
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.SyncRepository
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel @Inject constructor(
        billingManager: BillingManager,
        markAllSeen: MarkAllSeen,
        migratePreferences: MigratePreferences,
        syncRepository: SyncRepository,
        private val conversationRepo: ConversationRepository,
        private val deleteConversations: DeleteConversations,
        private val markArchived: MarkArchived,
        private val markBlocked: MarkBlocked,
        private val markPinned: MarkPinned,
        private val markRead: MarkRead,
        private val markUnarchived: MarkUnarchived,
        private val markUnpinned: MarkUnpinned,
        private val markUnread: MarkUnread,
        private val navigator: Navigator,
        private val permissionManager: PermissionManager,
        private val prefs: Preferences,
        private val ratingManager: RatingManager,
        private val syncMessages: SyncMessages
) : QkViewModel<MainView, MainState>(MainState(page = Inbox(data = conversationRepo.getConversations()))) {

    init {
        disposables += deleteConversations
        disposables += markAllSeen
        disposables += markArchived
        disposables += markUnarchived
        disposables += migratePreferences
        disposables += syncMessages

        // Show the syncing UI
        disposables += syncRepository.syncProgress
                .distinctUntilChanged()
                .subscribe { syncing -> newState { copy(syncing = syncing) } }

        // Update the upgraded status
        disposables += billingManager.upgradeStatus
                .subscribe { upgraded -> newState { copy(upgraded = upgraded) } }

        // Show the rating UI
        disposables += ratingManager.shouldShowRating
                .subscribe { show -> newState { copy(showRating = show) } }


        // Migrate the preferences from 2.7.3
        migratePreferences.execute(Unit)


        // If we have all permissions and we've never run a sync, run a sync. This will be the case
        // when upgrading from 2.7.3, or if the app's data was cleared
        val lastSync = Realm.getDefaultInstance().use { realm -> realm.where(SyncLog::class.java)?.max("date") ?: 0 }
        if (lastSync == 0 && permissionManager.isDefaultSms() && permissionManager.hasSmsAndContacts()) {
            syncMessages.execute(Unit)
        }

        ratingManager.addSession()
        markAllSeen.execute(Unit)
    }

    override fun bindView(view: MainView) {
        super.bindView(view)

        if (!permissionManager.hasSmsAndContacts()) {
            view.requestPermissions()
        }

        // If the default SMS state or permission states change, update the ViewState
        Observables.combineLatest(
                view.activityResumedIntent.map { permissionManager.isDefaultSms() }.distinctUntilChanged(),
                view.activityResumedIntent.map { permissionManager.hasSms() }.distinctUntilChanged(),
                view.activityResumedIntent.map { permissionManager.hasContacts() }.distinctUntilChanged())
        { defaultSms, smsPermission, contactPermission ->
            newState { copy(defaultSms = defaultSms, smsPermission = smsPermission, contactPermission = contactPermission) }
        }
                .autoDisposable(view.scope())
                .subscribe()

        // If the SMS permission state changes from false to true, sync messages
        view.activityResumedIntent
                .map { permissionManager.hasSms() }
                .distinctUntilChanged()
                .skip(1)
                .filter { hasSms -> hasSms }
                .take(1)
                .autoDisposable(view.scope())
                .subscribe {
                    syncMessages.execute(Unit)
                    if (!permissionManager.isDefaultSms()) {
                        navigator.showDefaultSmsDialog()
                    }
                }

        view.queryChangedIntent
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map { query -> query.removeAccents() }
                .withLatestFrom(state) { query, state ->
                    if (query.isEmpty() && state.page is Searching) {
                        newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                    }
                    query
                }
                .filter { query -> query.length >= 2 }
                .doOnNext {
                    newState {
                        val page = (page as? Searching) ?: Searching()
                        copy(page = page.copy(loading = true))
                    }
                }
                .observeOn(Schedulers.io())
                .switchMap { query -> Observable.just(query).map { conversationRepo.searchConversations(it) } }
                .autoDisposable(view.scope())
                .subscribe { data -> newState { copy(page = Searching(loading = false, data = data)) } }

        view.composeIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showCompose() }

        view.homeIntent
                .withLatestFrom(state) { _, state ->
                    when {
                        state.page is Searching -> view.clearSearch()

                        state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                        state.page is Inbox && state.page.showClearButton -> view.clearSearch()

                        state.page is Archived && state.page.selected > 0 -> view.clearSelection()

                        else -> newState { copy(drawerOpen = true) }
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.drawerOpenIntent
                .autoDisposable(view.scope())
                .subscribe { open -> newState { copy(drawerOpen = open) } }

        view.drawerItemIntent
                .doOnNext { newState { copy(drawerOpen = false) } }
                .doOnNext { if (it == DrawerItem.BACKUP) navigator.showBackup() }
                .doOnNext { if (it == DrawerItem.SCHEDULED) navigator.showScheduled() }
                .doOnNext { if (it == DrawerItem.BLOCKING) navigator.showBlockedConversations() }
                .doOnNext { if (it == DrawerItem.SETTINGS) navigator.showSettings() }
                .doOnNext { if (it == DrawerItem.PLUS) navigator.showQksmsPlusActivity("main_menu") }
                .doOnNext { if (it == DrawerItem.HELP) navigator.showSupport() }
                .doOnNext { if (it == DrawerItem.INVITE) navigator.showInvite() }
                .distinctUntilChanged()
                .doOnNext {
                    when (it) {
                        DrawerItem.INBOX -> newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                        DrawerItem.ARCHIVED -> newState { copy(page = Archived(data = conversationRepo.getConversations(true))) }
                        else -> {
                        } // Do nothing
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .withLatestFrom(view.conversationsSelectedIntent) { itemId, conversations ->
                    when (itemId) {
                        R.id.archive -> {
                            markArchived.execute(conversations)
                            view.clearSelection()
                        }

                        R.id.unarchive -> {
                            markUnarchived.execute(conversations)
                            view.clearSelection()
                        }

                        R.id.delete -> view.showDeleteDialog(conversations)

                        R.id.pin -> {
                            markPinned.execute(conversations)
                            view.clearSelection()
                        }

                        R.id.unpin -> {
                            markUnpinned.execute(conversations)
                            view.clearSelection()
                        }

                        R.id.read -> {
                            markRead.execute(conversations)
                            view.clearSelection()
                        }

                        R.id.unread -> {
                            markUnread.execute(conversations)
                            view.clearSelection()
                        }

                        R.id.block -> {
                            markBlocked.execute(conversations)
                            view.clearSelection()
                        }
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.plusBannerIntent
                .autoDisposable(view.scope())
                .subscribe {
                    newState { copy(drawerOpen = false) }
                    navigator.showQksmsPlusActivity("main_banner")
                }

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
                .withLatestFrom(state) { selection, state ->
                    val pin = selection
                            .mapNotNull(conversationRepo::getConversation)
                            .sumBy { if (it.pinned) -1 else 1 } >= 0
                    val read = selection
                            .mapNotNull(conversationRepo::getConversation)
                            .sumBy { if (it.read) -1 else 1 } >= 0
                    val selected = selection.size

                    when (state.page) {
                        is Inbox -> {
                            val page = state.page.copy(markPinned = pin, markRead = read, selected = selected, showClearButton = selected > 0)
                            newState { copy(page = page.copy(markRead = read, selected = selected, showClearButton = selected > 0)) }
                        }

                        is Archived -> {
                            val page = state.page.copy(markPinned = pin, markRead = read, selected = selected, showClearButton = selected > 0)
                            newState { copy(page = page) }
                        }
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        // Delete the conversation
        view.confirmDeleteIntent
                .autoDisposable(view.scope())
                .subscribe { conversations ->
                    deleteConversations.execute(conversations)
                    view.clearSelection()
                }

        view.swipeConversationIntent
                .autoDisposable(view.scope())
                .subscribe { (threadId, direction) ->
                    val action = if (direction == ItemTouchHelper.RIGHT) prefs.swipeRight.get() else prefs.swipeLeft.get()
                    when (action) {
                        Preferences.SWIPE_ACTION_ARCHIVE -> markArchived.execute(listOf(threadId)) { view.showArchivedSnackbar() }
                        Preferences.SWIPE_ACTION_DELETE -> view.showDeleteDialog(listOf(threadId))
                        Preferences.SWIPE_ACTION_CALL -> conversationRepo.getConversation(threadId)?.recipients?.firstOrNull()?.address?.let(navigator::makePhoneCall)
                        Preferences.SWIPE_ACTION_READ -> markRead.execute(listOf(threadId))
                    }
                }

        view.undoArchiveIntent
                .withLatestFrom(view.swipeConversationIntent) { _, pair -> pair.first }
                .autoDisposable(view.scope())
                .subscribe { threadId -> markUnarchived.execute(listOf(threadId)) }

        view.snackbarButtonIntent
                .withLatestFrom(state) { _, state ->
                    when {
                        !state.smsPermission -> view.requestPermissions()
                        !state.defaultSms -> navigator.showDefaultSmsDialog()
                        !state.contactPermission -> view.requestPermissions()
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.backPressedIntent
                .withLatestFrom(state) { _, state ->
                    when {
                        state.drawerOpen -> newState { copy(drawerOpen = false) }

                        state.page is Searching -> view.clearSearch()

                        state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                        state.page is Inbox && state.page.showClearButton -> view.clearSearch()

                        state.page is Archived && state.page.selected > 0 -> view.clearSelection()

                        state.page !is Inbox -> newState { copy(page = Inbox(data = conversationRepo.getConversations())) }

                        else -> newState { copy(hasError = true) }
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()
    }

}