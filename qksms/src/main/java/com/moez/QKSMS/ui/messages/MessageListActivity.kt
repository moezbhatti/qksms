package com.moez.QKSMS.ui.messages

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.moez.QKSMS.R
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.dagger.DaggerMessagesComponent
import com.moez.QKSMS.dagger.MessagesModule
import com.moez.QKSMS.ui.base.QkActivity
import kotlinx.android.synthetic.main.message_list_activity.*
import javax.inject.Inject

class MessageListActivity : QkActivity() {

    @Inject lateinit var viewModel: MessageListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_list_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val threadId = intent.getLongExtra("thread_id", 0)
        DaggerMessagesComponent.builder()
                .appComponent(AppComponentManager.appComponent)
                .messagesModule(MessagesModule(threadId))
                .build()
                .inject(this)

        viewModel.conversation.addChangeListener { realmResults ->
            if (realmResults.size > 0) {
                title = realmResults[0]?.getTitle()
            } else {
                finish()
            }
        }

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        messageList.layoutManager = layoutManager
        messageList.adapter = MessageAdapter(this, viewModel.messages)
    }

}