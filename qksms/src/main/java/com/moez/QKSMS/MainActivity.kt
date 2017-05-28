package com.moez.QKSMS

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.ui.conversations.ConversationAdapter
import com.moez.QKSMS.util.SyncManager
import io.realm.Realm

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Realm.init(this)

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_SMS)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        Log.d(TAG, "Permission granted")
                        SyncManager.dumpColumns(this@MainActivity)
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        Log.d(TAG, "Permission denied")
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                    }
                })
                .check()

        val realmResults = Realm.getDefaultInstance().where(Conversation::class.java).findAll()

        val conversations = findViewById(R.id.conversation_list) as RecyclerView
        conversations.layoutManager = LinearLayoutManager(this)
        conversations.adapter = ConversationAdapter(this, realmResults)

    }
}
