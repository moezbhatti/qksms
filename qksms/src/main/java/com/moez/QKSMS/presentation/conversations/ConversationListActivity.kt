package com.moez.QKSMS.presentation.conversations

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.moez.QKSMS.R
import com.moez.QKSMS.presentation.base.QkActivity
import com.moez.QKSMS.presentation.messages.MessageListActivity
import kotlinx.android.synthetic.main.conversation_list_activity.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class ConversationListActivity : QkActivity(), Observer<ConversationListViewState> {

    private lateinit var viewModel: ConversationListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.conversation_list_activity)
        ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0).syncState()

        onNewIntent(intent)
        requestPermissions()

        viewModel = ViewModelProviders.of(this)[ConversationListViewModel::class.java]
        viewModel.state.observe(this, this)

        conversationList.layoutManager = LinearLayoutManager(this)

        swipeRefresh.setOnRefreshListener { viewModel.onRefresh() }
        RxView.clicks(compose).subscribe {
            // TODO: add entry point to compose view here
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getLongExtra("threadId", 0)?.takeIf { threadId -> threadId > 0 }?.let { threadId ->
            startActivity(Intent(this, MessageListActivity::class.java).putExtra("threadId", threadId))
        }
    }

    override fun onChanged(state: ConversationListViewState?) {
        state?.let {
            if (conversationList.adapter == null && state.conversations?.isValid == true) {
                conversationList.adapter = ConversationAdapter(state.conversations)
            }

            swipeRefresh.isRefreshing = state.refreshing
        }
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
