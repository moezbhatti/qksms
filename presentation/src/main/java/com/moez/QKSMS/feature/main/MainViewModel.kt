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
import com.moez.QKSMS.common.base.QkViewModel
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.interactor.DeleteConversations
import com.moez.QKSMS.interactor.MarkAllSeen
import com.moez.QKSMS.interactor.MarkArchived
import com.moez.QKSMS.interactor.MarkPinned
import com.moez.QKSMS.interactor.MarkRead
import com.moez.QKSMS.interactor.MarkUnarchived
import com.moez.QKSMS.interactor.MarkUnpinned
import com.moez.QKSMS.interactor.MarkUnread
import com.moez.QKSMS.interactor.MigratePreferences
import com.moez.QKSMS.interactor.SyncContacts
import com.moez.QKSMS.interactor.SyncMessages
import com.moez.QKSMS.listener.ContactAddedListener
import com.moez.QKSMS.manager.BillingManager
import com.moez.QKSMS.manager.ChangelogManager
import com.moez.QKSMS.manager.PermissionManager
import com.moez.QKSMS.manager.RatingManager
import com.moez.QKSMS.model.SyncLog
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.SyncRepository
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel @Inject constructor(
    billingManager: BillingManager,
    contactAddedListener: ContactAddedListener,
    markAllSeen: MarkAllSeen,
    migratePreferences: MigratePreferences,
    syncRepository: SyncRepository,
    private val changelogManager: ChangelogManager,
    private val conversationRepo: ConversationRepository,
    private val deleteConversations: DeleteConversations,
    private val markArchived: MarkArchived,
    private val markPinned: MarkPinned,
    private val markRead: MarkRead,
    private val markUnarchived: MarkUnarchived,
    private val markUnpinned: MarkUnpinned,
    private val markUnread: MarkUnread,
    private val navigator: Navigator,
    private val permissionManager: PermissionManager,
    private val prefs: Preferences,
    private val ratingManager: RatingManager,
    private val syncContacts: SyncContacts,
    private val syncMessages: SyncMessages
) : QkViewModel<MainView, MainState>(MainState(page = Inbox(data = conversationRepo.getConversations()))) {

    init {
        disposables += deleteConversations
        disposables += markAllSeen
        disposables += markArchived
        disposables += markUnarchived
        disposables += migratePreferences
        disposables += syncContacts
        disposables += syncMessages

        // Show the syncing UI
        disposables += syncRepository.syncProgress
                .sample(16, TimeUnit.MILLISECONDS)
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
        if (lastSync == 0 && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
            syncMessages.execute(Unit)
        }

        // Sync contacts when we detect a change
        if (permissionManager.hasContacts()) {
            disposables += contactAddedListener.listen()
                    .debounce(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribe { syncContacts.execute(Unit) }
        }

        ratingManager.addSession()
        markAllSeen.execute(Unit)
    }

    override fun bindView(view: MainView) {
        super.bindView(view)

        when {
            !permissionManager.isDefaultSms() -> view.requestDefaultSms()
            !permissionManager.hasReadSms() || !permissionManager.hasContacts() -> view.requestPermissions()
        }

        val permissions = view.activityResumedIntent
                .filter { resumed -> resumed }
                .observeOn(Schedulers.io())
                .map { Triple(permissionManager.isDefaultSms(), permissionManager.hasReadSms(), permissionManager.hasContacts()) }
                .distinctUntilChanged()
                .share()

        // If the default SMS state or permission states change, update the ViewState
        permissions
                .doOnNext { (defaultSms, smsPermission, contactPermission) ->
                    newState { copy(defaultSms = defaultSms, smsPermission = smsPermission, contactPermission = contactPermission) }
                }
                .autoDisposable(view.scope())
                .subscribe()

        // If we go from not having all permissions to having them, sync messages
        permissions
                .skip(1)
                .filter { it.first && it.second && it.third }
                .take(1)
                .autoDisposable(view.scope())
                .subscribe { syncMessages.execute(Unit) }

        // Launch screen from intent
        view.onNewIntentIntent
                .autoDisposable(view.scope())
                .subscribe { intent ->
                    when (intent.getStringExtra("screen")) {
                        "compose" -> navigator.showConversation(intent.getLongExtra("threadId", 0))
                        "blocking" -> navigator.showBlockedConversations()
                    }
                }

        // Show changelog
        if (changelogManager.didUpdate()) {
            if (Locale.getDefault().language.startsWith("en")) {
                GlobalScope.launch(Dispatchers.Main) {
                    val changelog = changelogManager.getChangelog()
                    changelogManager.markChangelogSeen()
                    view.showChangelog(changelog)
                }
            } else {
                changelogManager.markChangelogSeen()
            }
        } else {
            changelogManager.markChangelogSeen()
        }

        view.changelogMoreIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showChangelog() }

        view.queryChangedIntent
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .withLatestFrom(state) { query, state ->
                    if (query.isEmpty() && state.page is Searching) {
                        newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                    }
                    query
                }
                .filter { query -> query.length >= 2 }
                .map { query -> query.trim() }
                .distinctUntilChanged()
                .doOnNext {
                    newState {
                        val page = (page as? Searching) ?: Searching()
                        copy(page = page.copy(loading = true))
                    }
                }
                .observeOn(Schedulers.io())
                .map(conversationRepo::searchConversations)
                .autoDisposable(view.scope())
                .subscribe { data -> newState { copy(page = Searching(loading = false, data = data)) } }

        view.activityResumedIntent
                .filter { resumed -> !resumed }
                .switchMap {
                    // Take until the activity is resumed
                    prefs.keyChanges
                            .filter { key -> key.contains("theme") }
                            .map { true }
                            .mergeWith(prefs.autoColor.asObservable().skip(1))
                            .doOnNext { view.themeChanged() }
                            .takeUntil(view.activityResumedIntent.filter { resumed -> resumed })
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.composeIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showCompose() }

        view.homeIntent
                .withLatestFrom(state) { _, state ->
                    when {
                        state.page is Searching -> view.clearSearch()
                        state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                        state.page is Archived && state.page.selected > 0 -> view.clearSelection()

                        else -> newState { copy(drawerOpen = true) }
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.drawerOpenIntent
                .autoDisposable(view.scope())
                .subscribe { open -> newState { copy(drawerOpen = open) } }

        view.navigationIntent
                .withLatestFrom(state) { drawerItem, state ->
                    newState { copy(drawerOpen = false) }
                    when (drawerItem) {
                        NavItem.BACK -> when {
                            state.drawerOpen -> Unit
                            state.page is Searching -> view.clearSearch()
                            state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                            state.page is Archived && state.page.selected > 0 -> view.clearSelection()
                            state.page !is Inbox -> {
                                newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                            }
                            else -> newState { copy(hasError = true) }
                        }
                        NavItem.BACKUP -> navigator.showBackup()
                        NavItem.SCHEDULED -> navigator.showScheduled()
                        NavItem.BLOCKING -> navigator.showBlockedConversations()
                        NavItem.SETTINGS -> navigator.showSettings()
                        NavItem.PLUS -> navigator.showQksmsPlusActivity("main_menu")
                        NavItem.HELP -> navigator.showSupport()
                        NavItem.INVITE -> navigator.showInvite()
                        else -> Unit
                    }
                    drawerItem
                }
                .distinctUntilChanged()
                .doOnNext { drawerItem ->
                    when (drawerItem) {
                        NavItem.INBOX -> newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                        NavItem.ARCHIVED -> newState { copy(page = Archived(data = conversationRepo.getConversations(true))) }
                        else -> Unit
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.archive }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markArchived.execute(conversations)
                    view.clearSelection()
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.unarchive }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markUnarchived.execute(conversations)
                    view.clearSelection()
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.delete }
                .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    view.showDeleteDialog(conversations)
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.add }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations -> conversations }
                .doOnNext { view.clearSelection() }
                .filter { conversations -> conversations.size == 1 }
                .map { conversations -> conversations.first() }
                .mapNotNull(conversationRepo::getConversation)
                .map { conversation -> conversation.recipients }
                .mapNotNull { recipients -> recipients[0]?.address?.takeIf { recipients.size == 1 } }
                .doOnNext(navigator::addContact)
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.pin }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markPinned.execute(conversations)
                    view.clearSelection()
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.unpin }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markUnpinned.execute(conversations)
                    view.clearSelection()
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.read }
                .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markRead.execute(conversations)
                    view.clearSelection()
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.unread }
                .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markUnread.execute(conversations)
                    view.clearSelection()
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.block }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    view.showBlockingDialog(conversations, true)
                    view.clearSelection()
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
                    val conversations = selection.mapNotNull(conversationRepo::getConversation)
                    val add = conversations.firstOrNull()
                            ?.takeIf { conversations.size == 1 }
                            ?.takeIf { conversation -> conversation.recipients.size == 1 }
                            ?.recipients?.first()
                            ?.takeIf { recipient -> recipient.contact == null } != null
                    val pin = conversations.sumBy { if (it.pinned) -1 else 1 } >= 0
                    val read = conversations.sumBy { if (!it.unread) -1 else 1 } >= 0
                    val selected = selection.size

                    when (state.page) {
                        is Inbox -> {
                            val page = state.page.copy(addContact = add, markPinned = pin, markRead = read, selected = selected)
                            newState { copy(page = page) }
                        }

                        is Archived -> {
                            val page = state.page.copy(addContact = add, markPinned = pin, markRead = read, selected = selected)
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
                        Preferences.SWIPE_ACTION_BLOCK -> view.showBlockingDialog(listOf(threadId), true)
                        Preferences.SWIPE_ACTION_CALL -> conversationRepo.getConversation(threadId)?.recipients?.firstOrNull()?.address?.let(navigator::makePhoneCall)
                        Preferences.SWIPE_ACTION_READ -> markRead.execute(listOf(threadId))
                        Preferences.SWIPE_ACTION_UNREAD -> markUnread.execute(listOf(threadId))
                    }
                }

        view.undoArchiveIntent
                .withLatestFrom(view.swipeConversationIntent) { _, pair -> pair.first }
                .autoDisposable(view.scope())
                .subscribe { threadId -> markUnarchived.execute(listOf(threadId)) }

        view.snackbarButtonIntent
                .withLatestFrom(state) { _, state ->
                    when {
                        !state.defaultSms -> view.requestDefaultSms()
                        !state.smsPermission -> view.requestPermissions()
                        !state.contactPermission -> view.requestPermissions()
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()
    }

}