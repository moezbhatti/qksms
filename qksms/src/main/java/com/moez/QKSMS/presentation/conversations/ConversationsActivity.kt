package com.moez.QKSMS.presentation.conversations

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
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
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.base.QkActivity
import kotlinx.android.synthetic.main.conversation_list_activity.*
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import javax.inject.Inject

class ConversationsActivity : QkActivity<ConversationsViewModel, ConversationsState>(), ConversationsView {

    @Inject lateinit var navigator: Navigator

    override val viewModelClass = ConversationsViewModel::class
    override val composeIntent by lazy { compose.clicks() }
    override val drawerOpenIntent by lazy { drawerLayout.drawerOpen(Gravity.START) }
    override val archivedIntent by lazy { archived.clicks() }
    override val scheduledIntent by lazy { scheduled.clicks() }
    override val blockedIntent by lazy { blocked.clicks() }
    override val settingsIntent by lazy { settings.clicks() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppComponentManager.appComponent.inject(this)
        setContentView(R.layout.conversation_list_activity)
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

    override fun render(state: ConversationsState) {
        if (conversationList.adapter != state.adapter) {
            conversationList.adapter = state.adapter
        }

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
