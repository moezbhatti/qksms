package com.moez.QKSMS.presentation.main

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Gravity
import com.jakewharton.rxbinding2.support.v4.widget.drawerOpen
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.common.base.QkActivity
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.toolbar_search.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : QkActivity<MainViewModel>(), MainView {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var conversationsAdapter: ConversationsAdapter
    @Inject lateinit var itemTouchCallback: ConversationItemTouchCallback

    override val viewModelClass = MainViewModel::class
    override val queryChangedIntent by lazy { toolbarSearch.textChanges() }
    override val composeIntent by lazy { compose.clicks() }
    override val drawerOpenIntent by lazy { drawerLayout.drawerOpen(Gravity.START) }
    override val drawerItemIntent: Observable<DrawerItem> by lazy {
        Observable.merge(listOf(
                inbox.clicks().map { DrawerItem.INBOX },
                archived.clicks().map { DrawerItem.ARCHIVED },
                scheduled.clicks().map { DrawerItem.SCHEDULED },
                blocked.clicks().map { DrawerItem.BLOCKED },
                settings.clicks().map { DrawerItem.SETTINGS }))
    }
    override val archiveConversationIntent: Subject<Long> = PublishSubject.create()
    override val unarchiveConversationIntent: Subject<Long> = PublishSubject.create()
    override val deleteConversationIntent: Subject<Long> = PublishSubject.create()
    override val swipeConversationIntent: Observable<Int> by lazy { itemTouchCallback.swipes }
    override val undoSwipeConversationIntent: Subject<Unit> = PublishSubject.create()

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
        ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0).syncState()
        requestPermissions()
        viewModel.bindView(this)
        toolbarSearch.setHint(R.string.title_conversations)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Don't allow clicks to pass through the drawer layout
        drawer.clicks().subscribe()

        disposables += colors.background
                .doOnNext { color -> window.decorView.setBackgroundColor(color) }
                .doOnNext { color -> drawer.setBackgroundColor(color) }
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

        disposables += colors.theme
                .doOnNext { color -> compose.setBackgroundTint(color) }
                .subscribe()

        disposables += Observables
                .combineLatest(colors.theme, colors.textSecondary, { theme, textSecondary ->
                    ColorStateList(states, intArrayOf(theme, textSecondary))
                })
                .doOnNext { tintList -> inboxIcon.imageTintList = tintList }
                .doOnNext { tintList -> archivedIcon.imageTintList = tintList }
                .doOnNext { tintList -> scheduledIcon.imageTintList = tintList }
                .doOnNext { tintList -> blockedIcon.imageTintList = tintList }
                .doOnNext { tintList -> settingsIcon.imageTintList = tintList }
                .subscribe()

        disposables += colors.separator
                .doOnNext { color -> inbox.background = rowBackground(color) }
                .doOnNext { color -> archived.background = rowBackground(color) }
                .doOnNext { color -> scheduled.background = rowBackground(color) }
                .doOnNext { color -> blocked.background = rowBackground(color) }
                .subscribe()

        conversationsAdapter.longClicks.subscribe { threadId ->
            AlertDialog.Builder(this)
                    .setItems(R.array.conversation_options, { _, row ->
                        when (row) {
                            0 -> archiveConversationIntent.onNext(threadId)
                            1 -> unarchiveConversationIntent.onNext(threadId)
                            2 -> deleteConversationIntent.onNext(threadId)
                        }
                    })
                    .show()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getLongExtra("threadId", 0)
                ?.takeIf { threadId -> threadId > 0 }
                ?.let { threadId -> navigator.showConversation(threadId) }
    }

    override fun render(state: MainState) {
        when (state.page) {
            is Inbox -> {
                if (recyclerView.adapter != conversationsAdapter) recyclerView.adapter = conversationsAdapter
                if (conversationsAdapter.flowable != state.page.data) conversationsAdapter.flowable = state.page.data
                itemTouchHelper.attachToRecyclerView(recyclerView)
            }

            is Archived -> {
                if (recyclerView.adapter != conversationsAdapter) recyclerView.adapter = conversationsAdapter
                if (conversationsAdapter.flowable != state.page.data) conversationsAdapter.flowable = state.page.data
                itemTouchHelper.attachToRecyclerView(null)
            }

            is Scheduled -> {
                recyclerView.adapter = null
                itemTouchHelper.attachToRecyclerView(null)
            }

            is Blocked -> {
                recyclerView.adapter = null
                itemTouchHelper.attachToRecyclerView(null)
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
    }

    private fun requestPermissions() {
        Dexter.withActivity(this)
                .withPermissions(arrayListOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS))
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionRationaleShouldBeShown(request: MutableList<PermissionRequest>, token: PermissionToken) {
                    }

                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        for (response in report.grantedPermissionResponses) Timber.v("Permission granted: ${response.permissionName}")
                        for (response in report.deniedPermissionResponses) Timber.v("Permission denied: ${response.permissionName}")
                    }
                })
                .check()
    }

}
