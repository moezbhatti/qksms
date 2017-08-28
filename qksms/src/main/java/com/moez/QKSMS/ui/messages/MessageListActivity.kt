package com.moez.QKSMS.ui.messages

import android.os.Bundle
import com.moez.QKSMS.R
import com.moez.QKSMS.dagger.DaggerMessagesComponent
import com.moez.QKSMS.dagger.MessagesModule
import com.moez.QKSMS.ui.base.QkActivity

class MessageListActivity : QkActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val threadId = intent.getLongExtra("thread_id", 0)

        val messagesComponent = DaggerMessagesComponent.builder()
                .appComponent(getAppComponent())
                .messagesModule(MessagesModule(threadId))
                .build()

        val fragment = MessageListFragment()
        messagesComponent.inject(fragment)

        supportFragmentManager.beginTransaction().replace(R.id.contentFrame, fragment).commit()
    }

}