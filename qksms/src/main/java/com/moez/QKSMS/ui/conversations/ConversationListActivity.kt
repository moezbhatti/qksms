package com.moez.QKSMS.ui.conversations

import android.Manifest
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.moez.QKSMS.R
import com.moez.QKSMS.ui.base.QkActivity
import kotlinx.android.synthetic.main.conversation_list_activity.*
import javax.inject.Inject

class ConversationListActivity : QkActivity() {
    val TAG = "MainActivity"

    @Inject lateinit var viewModel: ConversationListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.conversation_list_activity)
        getAppComponent()?.inject(this)

        conversationList.layoutManager = LinearLayoutManager(this)
        conversationList.adapter = ConversationAdapter(this, viewModel.conversations)

        swipeRefresh.setOnRefreshListener {
            viewModel.onRefresh { swipeRefresh.isRefreshing = false }
        }

        requestSmsPermission()
    }

    private fun requestSmsPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_SMS)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        Log.d(TAG, "Permission granted")
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        Log.d(TAG, "Permission denied")
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                    }
                })
                .check()
    }
}
