package com.moez.QKSMS.ui.conversations

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.moez.QKSMS.R
import com.moez.QKSMS.ui.base.QkActivity
import kotlinx.android.synthetic.main.conversation_list_activity.*
import timber.log.Timber

class ConversationListActivity : QkActivity() {

    lateinit var viewModel: ConversationListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.conversation_list_activity)

        viewModel = ViewModelProviders.of(this)[ConversationListViewModel::class.java]

        conversationList.layoutManager = LinearLayoutManager(this)
        conversationList.adapter = ConversationAdapter(this, viewModel.conversations)

        swipeRefresh.setOnRefreshListener {
            viewModel.onRefresh { swipeRefresh.isRefreshing = false }
        }

        requestPermissions()
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
