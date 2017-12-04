package com.moez.QKSMS.presentation.main

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Gravity
import com.jakewharton.rxbinding2.support.v4.widget.drawerOpen
import com.jakewharton.rxbinding2.view.clicks
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.base.QkActivity
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : QkActivity<MainViewModel>(), MainView {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var itemTouchCallback: ConversationItemTouchCallback

    override val viewModelClass = MainViewModel::class
    override val composeIntent by lazy { compose.clicks() }
    override val drawerOpenIntent by lazy { drawerLayout.drawerOpen(Gravity.START) }
    override val inboxIntent by lazy { inbox.clicks() }
    override val archivedIntent by lazy { archived.clicks() }
    override val scheduledIntent by lazy { scheduled.clicks() }
    override val blockedIntent by lazy { blocked.clicks() }
    override val settingsIntent by lazy { settings.clicks() }
    override val conversationSwipedIntent by lazy { itemTouchCallback.swipes }

    private val itemTouchHelper by lazy { ItemTouchHelper(itemTouchCallback) }

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0).syncState()
        requestPermissions()
        viewModel.bindView(this)

        conversationList.layoutManager = LinearLayoutManager(this)

        // Don't allow clicks to pass through the drawer layout
        drawer.clicks().subscribe()

        val states = arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf(-android.R.attr.state_selected))

        val rowBackground = {
            StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_selected), getDrawable(R.color.row_selected))
                addState(intArrayOf(-android.R.attr.state_selected), getDrawable(R.drawable.ripple))
                mutate()
            }
        }

        disposables += themeManager.color
                .doOnNext { color -> compose.setBackgroundTint(color) }
                .map { color -> ColorStateList(states, intArrayOf(color, themeManager.textSecondary)) }
                .doOnNext { tintList -> inboxIcon.imageTintList = tintList }
                .doOnNext { tintList -> archivedIcon.imageTintList = tintList }
                .doOnNext { tintList -> scheduledIcon.imageTintList = tintList }
                .doOnNext { tintList -> blockedIcon.imageTintList = tintList }
                .subscribe()

        inbox.background = rowBackground()
        archived.background = rowBackground()
        scheduled.background = rowBackground()
        blocked.background = rowBackground()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getLongExtra("threadId", 0)
                ?.takeIf { threadId -> threadId > 0 }
                ?.let { threadId -> navigator.showConversation(threadId) }
    }

    override fun render(state: MainState) {
        if (conversationList.adapter != state.adapter) {
            conversationList.adapter = state.adapter
            itemTouchHelper.attachToRecyclerView(if (state.page == MainPage.INBOX) conversationList else null)
        }

        inbox.isSelected = state.page == MainPage.INBOX
        inboxIcon.isSelected = state.page == MainPage.INBOX
        archived.isSelected = state.page == MainPage.ARCHIVED
        archivedIcon.isSelected = state.page == MainPage.ARCHIVED
        scheduled.isSelected = state.page == MainPage.SCHEDULED
        scheduledIcon.isSelected = state.page == MainPage.SCHEDULED
        blocked.isSelected = state.page == MainPage.BLOCKED
        blockedIcon.isSelected = state.page == MainPage.BLOCKED

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
