package com.moez.QKSMS

import android.Manifest
import android.os.Bundle
import android.util.Log
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.moez.QKSMS.ui.base.QkActivity
import com.moez.QKSMS.ui.conversations.ConversationListFragment

class MainActivity : QkActivity() {
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.conversation_list_fragment)

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

        supportFragmentManager.beginTransaction().replace(R.id.contentFrame, ConversationListFragment()).commit()
    }
}
