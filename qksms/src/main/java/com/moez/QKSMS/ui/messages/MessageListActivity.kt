package com.moez.QKSMS.ui.messages

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.data.sync.MessageSyncManager
import com.moez.QKSMS.model.Message
import io.realm.Realm

class MessageListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.messages_fragment)

        val threadId = intent.getLongExtra("thread_id", 0)

        MessageSyncManager.copyToRealm(this, threadId)

        val realmResults = Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .findAll()

        val messages = findViewById(R.id.message_list) as RecyclerView
        messages.layoutManager = LinearLayoutManager(this)
        messages.adapter = MessageAdapter(this, realmResults)
    }

}