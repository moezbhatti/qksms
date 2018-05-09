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
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.jakewharton.rxbinding2.support.v4.widget.drawerOpen
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.Navigator
import common.base.QkThemedActivity
import common.util.extensions.autoScrollToStart
import common.util.extensions.dismissKeyboard
import common.util.extensions.setBackgroundTint
import common.util.extensions.setTint
import common.util.extensions.setVisible
import feature.conversations.ConversationItemTouchCallback
import feature.conversations.ConversationsAdapter
import injection.appComponent
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.main_activity.*
import repository.SyncRepository
import javax.inject.Inject

class MainActivity : QkThemedActivity<MainViewModel>(), MainView {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var conversationsAdapter: ConversationsAdapter
    @Inject lateinit var itemTouchCallback: ConversationItemTouchCallback

    override val viewModelClass = MainViewModel::class
    override val activityResumedIntent: Subject<Unit> = PublishSubject.create()
    override val queryChangedIntent by lazy { toolbarSearch.textChanges() }
    override val composeIntent by lazy { compose.clicks() }
    override val drawerOpenIntent: Observable<Boolean> by lazy {
        drawerLayout
                .drawerOpen(Gravity.START)
                .doOnNext { dismissKeyboard() }
    }
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override val drawerItemIntent: Observable<DrawerItem> by lazy {
        Observable.merge(listOf(
                inbox.clicks().map { DrawerItem.INBOX },
                archived.clicks().map { DrawerItem.ARCHIVED },
                scheduled.clicks().map { DrawerItem.SCHEDULED },
                settings.clicks().map { DrawerItem.SETTINGS },
                plus.clicks().map { DrawerItem.PLUS },
                help.clicks().map { DrawerItem.HELP }))
    }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val dismissRatingIntent by lazy { rateDismiss.clicks() }
    override val rateIntent by lazy { rateOkay.clicks() }
    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<Unit> = PublishSubject.create()
    override val swipeConversationIntent by lazy { itemTouchCallback.swipes }
    override val undoSwipeConversationIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent by lazy { snackbarButton.clicks() }
    override val backPressedIntent: Subject<Unit> = PublishSubject.create()

    private val toggle by lazy { ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0) }
    private val itemTouchHelper by lazy { ItemTouchHelper(itemTouchCallback) }
    private val archiveSnackbar by lazy {
        Snackbar.make(drawerLayout, R.string.toast_archived, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(R.string.button_undo, { undoSwipeConversationIntent.onNext(Unit) })
        }
    }

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        viewModel.bindView(this)

        toggle.syncState()
        toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            homeIntent.onNext(Unit)
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Don't allow clicks to pass through the drawer layout
        drawer.clicks().subscribe()

        scheduled.isEnabled = false

        colors.background
                .doOnNext { color -> window.decorView.setBackgroundColor(color) }
                .doOnNext { color -> drawer.setBackgroundColor(color) }
                .autoDisposable(scope())
                .subscribe()

        val states = arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf(-android.R.attr.state_selected))

        val rowBackground = { separator: Int ->
            StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_selected), ColorDrawable(separator))
                addState(intArrayOf(-android.R.attr.state_selected), getDrawable(R.drawable.ripple))
                mutate()
            }
        }

        // Set the theme color tint to the progressbar and FAB
        colors.theme
                .doOnNext { color -> syncingProgress.indeterminateTintList = ColorStateList.valueOf(color) }
                .doOnNext { color -> itemTouchCallback.color = color }
                .doOnNext { color -> rateIcon.setTint(color) }
                .doOnNext { color -> compose.setBackgroundTint(color) }
                .autoDisposable(scope())
                .subscribe()

        // Set the FAB compose icon color
        colors.textPrimaryOnTheme
                .doOnNext { color -> compose.setTint(color) }
                .doOnNext { color -> itemTouchCallback.iconColor = color }
                .autoDisposable(scope())
                .subscribe()

        // Set the hamburger icon color
        colors.textSecondary
                .autoDisposable(scope())
                .subscribe { color -> toggle.drawerArrowDrawable.color = color }

        // Set the color for the drawer icons
        Observables
                .combineLatest(colors.theme, colors.textSecondary, { theme, textSecondary ->
                    ColorStateList(states, intArrayOf(theme, textSecondary))
                })
                .doOnNext { tintList -> inboxIcon.imageTintList = tintList }
                .doOnNext { tintList -> archivedIcon.imageTintList = tintList }
                .doOnNext { tintList -> scheduledIcon.imageTintList = tintList }
                .doOnNext { tintList -> settingsIcon.imageTintList = tintList }
                .doOnNext { tintList -> plusIcon.imageTintList = tintList }
                .doOnNext { tintList -> helpIcon.imageTintList = tintList }
                .autoDisposable(scope())
                .subscribe()

        // Set the background highlight for the drawer options
        colors.separator
                .doOnNext { color -> inbox.background = rowBackground(color) }
                .doOnNext { color -> archived.background = rowBackground(color) }
                .doOnNext { color -> scheduled.background = rowBackground(color) }
                .doOnNext { color -> rateLayout.setBackgroundTint(color) }
                .autoDisposable(scope())
                .subscribe()

        conversationsAdapter.autoScrollToStart(recyclerView)
        conversationsAdapter.emptyView = empty
    }

    override fun render(state: MainState) {
        if (state.hasError) {
            finish()
            return
        }

        val selectedConversations = when (state.page) {
            is Inbox -> state.page.selected
            is Archived -> state.page.selected
            else -> 0
        }

        toolbarSearch.setVisible(state.page is Inbox && state.page.selected == 0)
        toolbarTitle.setVisible(toolbarSearch.visibility != View.VISIBLE)

        toolbar.menu.findItem(R.id.archive)?.isVisible = state.page is Inbox && selectedConversations != 0
        toolbar.menu.findItem(R.id.unarchive)?.isVisible = state.page is Archived && selectedConversations != 0
        toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0

        rateLayout.setVisible(state.showRating)

        compose.setVisible(state.page is Inbox || state.page is Archived)

        when (state.page) {
            is Inbox -> {
                showBackButton(state.page.showClearButton)
                title = getString(R.string.main_title_selected, state.page.selected)
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter = conversationsAdapter
                conversationsAdapter.flowable = state.page.data
                itemTouchHelper.attachToRecyclerView(recyclerView)
                empty.setText(when (state.page.showClearButton) {
                    true -> R.string.inbox_search_empty_text
                    false -> R.string.inbox_empty_text
                })
            }

            is Archived -> {
                showBackButton(state.page.showClearButton)
                title = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> getString(R.string.title_archived)
                }
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter = conversationsAdapter
                conversationsAdapter.flowable = state.page.data
                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.archived_empty_text)
            }

            is Scheduled -> {
                setTitle(R.string.title_scheduled)
                recyclerView.adapter = null
                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.scheduled_empty_text)
            }
        }

        when (state.page is Inbox && state.page.showArchivedSnackbar) {
            true -> archiveSnackbar.show()
            false -> archiveSnackbar.dismiss()
        }

        inbox.isSelected = state.page is Inbox
        archived.isSelected = state.page is Archived
        scheduled.isSelected = state.page is Scheduled

        if (drawerLayout.isDrawerOpen(Gravity.START) && !state.drawerOpen) drawerLayout.closeDrawer(Gravity.START)
        else if (!drawerLayout.isDrawerVisible(Gravity.START) && state.drawerOpen) drawerLayout.openDrawer(Gravity.START)

        syncing.setVisible(state.syncing is SyncRepository.SyncProgress.Running)
        snackbar.setVisible(state.syncing is SyncRepository.SyncProgress.Idle
                && !state.defaultSms || !state.smsPermission || !state.contactPermission)

        when {
            !state.smsPermission -> {
                snackbarTitle.setText(R.string.main_permission_required)
                snackbarMessage.setText(R.string.main_permission_sms)
                snackbarButton.setText(R.string.main_permission_allow)
            }

            !state.defaultSms -> {
                snackbarTitle.setText(R.string.main_default_sms_title)
                snackbarMessage.setText(R.string.main_default_sms_message)
                snackbarButton.setText(R.string.main_default_sms_change)
            }

            !state.contactPermission -> {
                snackbarTitle.setText(R.string.main_permission_required)
                snackbarMessage.setText(R.string.main_permission_contacts)
                snackbarButton.setText(R.string.main_permission_allow)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityResumedIntent.onNext(Unit)
    }

    override fun showBackButton(show: Boolean) {
        toggle.onDrawerSlide(drawer, if (show) 1f else 0f)
    }

    override fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS), 0)
    }

    override fun clearSearch() {
        dismissKeyboard()
        toolbarSearch.text = null
    }

    override fun clearSelection() {
        conversationsAdapter.clearSelection()
    }

    override fun showDeleteDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.button_delete, { _, _ -> confirmDeleteIntent.onNext(Unit) })
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun onBackPressed() {
        backPressedIntent.onNext(Unit)
    }

}
