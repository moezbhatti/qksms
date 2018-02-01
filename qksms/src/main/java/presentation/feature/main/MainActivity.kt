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
package presentation.feature.main

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import com.jakewharton.rxbinding2.support.v4.widget.drawerOpen
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.extensions.dpToPx
import common.util.extensions.setBackgroundTint
import common.util.extensions.setVisible
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.toolbar_search.*
import presentation.common.MenuItemAdapter
import presentation.common.Navigator
import presentation.common.base.QkActivity
import presentation.feature.conversations.ConversationItemTouchCallback
import presentation.feature.conversations.ConversationsAdapter
import javax.inject.Inject

class MainActivity : QkActivity<MainViewModel>(), MainView {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var conversationsAdapter: ConversationsAdapter
    @Inject lateinit var itemTouchCallback: ConversationItemTouchCallback
    @Inject lateinit var menuItemAdapter: MenuItemAdapter

    override val viewModelClass = MainViewModel::class
    override val queryChangedIntent by lazy { toolbarSearch.textChanges() }
    override val queryCancelledIntent: PublishSubject<Unit> = PublishSubject.create()
    override val composeIntent by lazy { compose.clicks() }
    override val drawerOpenIntent by lazy { drawerLayout.drawerOpen(Gravity.START) }
    override val drawerItemIntent: Observable<DrawerItem> by lazy {
        Observable.merge(listOf(
                inbox.clicks().map { DrawerItem.INBOX },
                archived.clicks().map { DrawerItem.ARCHIVED },
                scheduled.clicks().map { DrawerItem.SCHEDULED },
                blocked.clicks().map { DrawerItem.BLOCKED },
                settings.clicks().map { DrawerItem.SETTINGS },
                plus.clicks().map { DrawerItem.PLUS },
                help.clicks().map { DrawerItem.HELP }))
    }
    override val conversationClickIntent by lazy { conversationsAdapter.clicks }
    override val conversationLongClickIntent by lazy { conversationsAdapter.longClicks }
    override val conversationMenuItemIntent by lazy { menuItemAdapter.menuItemClicks }
    override val swipeConversationIntent by lazy { itemTouchCallback.swipes }
    override val undoSwipeConversationIntent: Subject<Unit> = PublishSubject.create()

    private val itemTouchHelper by lazy { ItemTouchHelper(itemTouchCallback) }
    private val archiveSnackbar by lazy {
        Snackbar.make(drawerLayout, R.string.toast_archived, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(R.string.button_undo, { undoSwipeConversationIntent.onNext(Unit) })
        }
    }
    private val conversationMenuDialog by lazy {
        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = menuItemAdapter
        recyclerView.setPadding(0, 8.dpToPx(this), 0, 8.dpToPx(this))
        AlertDialog.Builder(this).setView(recyclerView).create().apply {
            setOnDismissListener { conversationMenuItemIntent.onNext(-1) }
        }
    }

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        viewModel.bindView(this)
        toolbarSearch.setHint(R.string.title_conversations)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0).apply { syncState() }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Don't allow clicks to pass through the drawer layout
        drawer.clicks().subscribe()

        scheduled.isEnabled = false
        blocked.isEnabled = false

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
                .doOnNext { color -> compose.setBackgroundTint(color) }
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
                .doOnNext { tintList -> blockedIcon.imageTintList = tintList }
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
                .doOnNext { color -> blocked.background = rowBackground(color) }
                .autoDisposable(scope())
                .subscribe()

        conversationsAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                // If we're at the top, scroll up to show new conversations
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (firstVisiblePosition == 0) {
                    recyclerView.scrollToPosition(positionStart)
                }
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                onItemRangeInserted(positionStart, itemCount)
            }
        })
    }

    override fun render(state: MainState) {
        toolbarSearch.isEnabled = state.page is Inbox
        toolbarSearch.textSize = if (state.page is Inbox) 16f else 20f

        toolbar.menu.findItem(R.id.clear)?.run {
            isVisible = state.page is Inbox && state.page.showClearButton
        }

        syncing.setVisible(state.syncing)
        recyclerView.setVisible(!state.syncing)

        when (state.page) {
            is Inbox -> {
                if (!inbox.isSelected) toolbarSearch.text = null
                if (recyclerView.adapter != conversationsAdapter) recyclerView.adapter = conversationsAdapter
                if (conversationsAdapter.flowable != state.page.data) conversationsAdapter.flowable = state.page.data
                itemTouchHelper.attachToRecyclerView(recyclerView)
                menuItemAdapter.data = state.page.menu
                empty.setText(R.string.inbox_empty_text)
                empty.setVisible(state.page.empty)
            }

            is Archived -> {
                if (!archived.isSelected) toolbarSearch.setText(R.string.title_archived)
                if (recyclerView.adapter != conversationsAdapter) recyclerView.adapter = conversationsAdapter
                if (conversationsAdapter.flowable != state.page.data) conversationsAdapter.flowable = state.page.data
                itemTouchHelper.attachToRecyclerView(null)
                menuItemAdapter.data = state.page.menu
                empty.setText(R.string.archived_empty_text)
                empty.setVisible(state.page.empty)
            }

            is Scheduled -> {
                if (!scheduled.isSelected) toolbarSearch.setText(R.string.title_scheduled)
                recyclerView.adapter = null
                itemTouchHelper.attachToRecyclerView(null)
                menuItemAdapter.data = ArrayList()
                empty.setText(R.string.scheduled_empty_text)
                empty.setVisible(state.page.empty)
            }

            is Blocked -> {
                if (!blocked.isSelected) toolbarSearch.setText(R.string.title_blocked)
                recyclerView.adapter = null
                itemTouchHelper.attachToRecyclerView(null)
                menuItemAdapter.data = ArrayList()
                empty.setText(R.string.blocked_empty_text)
                empty.setVisible(state.page.empty)
            }
        }

        when (state.page is Inbox && state.page.showArchivedSnackbar) {
            true -> archiveSnackbar.show()
            false -> archiveSnackbar.dismiss()
        }

        inbox.isSelected = state.page is Inbox
        inboxIcon.isSelected = state.page is Inbox
        archived.isSelected = state.page is Archived
        archivedIcon.isSelected = state.page is Archived
        scheduled.isSelected = state.page is Scheduled
        scheduledIcon.isSelected = state.page is Scheduled
        blocked.isSelected = state.page is Blocked
        blockedIcon.isSelected = state.page is Blocked

        if (drawerLayout.isDrawerOpen(Gravity.START) && !state.drawerOpen) drawerLayout.closeDrawer(Gravity.START)
        else if (!drawerLayout.isDrawerVisible(Gravity.START) && state.drawerOpen) drawerLayout.openDrawer(Gravity.START)

        if (conversationMenuDialog.isShowing && menuItemAdapter.data.isEmpty()) conversationMenuDialog.dismiss()
        else if (!conversationMenuDialog.isShowing && menuItemAdapter.data.isNotEmpty()) conversationMenuDialog.show()
    }

    override fun clearSearch() {
        toolbarSearch.text = null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear -> queryCancelledIntent.onNext(Unit)
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

}
