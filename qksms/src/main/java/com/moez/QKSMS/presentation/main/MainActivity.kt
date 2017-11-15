package com.moez.QKSMS.presentation.main

import android.Manifest
import android.content.Intent
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
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.base.QkActivity
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : QkActivity<MainViewModel, MainState>(), MainView {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var themeManager: ThemeManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppComponentManager.appComponent.inject(this)
        setContentView(R.layout.main_activity)
        ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0).syncState()
        requestPermissions()
        viewModel.setView(this)

        conversationList.layoutManager = LinearLayoutManager(this)

        // Don't allow clicks to pass through the drawer layout
        drawer.clicks().subscribe()
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

        inbox.setBackgroundResource(getRowBackground(state.page == MainPage.INBOX))
        inboxIcon.setTint(getIconColor(state.page == MainPage.INBOX))
        archived.setBackgroundResource(getRowBackground(state.page == MainPage.ARCHIVED))
        archivedIcon.setTint(getIconColor(state.page == MainPage.ARCHIVED))
        scheduled.setBackgroundResource(getRowBackground(state.page == MainPage.SCHEDULED))
        scheduledIcon.setTint(getIconColor(state.page == MainPage.SCHEDULED))
        blocked.setBackgroundResource(getRowBackground(state.page == MainPage.BLOCKED))
        blockedIcon.setTint(getIconColor(state.page == MainPage.BLOCKED))

        if (drawerLayout.isDrawerOpen(Gravity.START) && !state.drawerOpen) drawerLayout.closeDrawer(Gravity.START)
        else if (!drawerLayout.isDrawerVisible(Gravity.START) && state.drawerOpen) drawerLayout.openDrawer(Gravity.START)
    }

    private fun getIconColor(selected: Boolean): Int {
        return if (selected) themeManager.color else themeManager.textSecondary
    }

    private fun getRowBackground(selected: Boolean): Int {
        return if (selected) R.color.row_selected else R.drawable.ripple
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
