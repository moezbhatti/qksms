package com.moez.QKSMS.ui.messages

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.moez.QKSMS.R
import com.moez.QKSMS.ui.base.QkActivity
import com.moez.QKSMS.util.observe
import kotlinx.android.synthetic.main.message_list_activity.*

class MessageListActivity : QkActivity(), Observer<MessageListViewState> {

    lateinit var viewModel: MessageListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_list_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this)[MessageListViewModel::class.java]
        viewModel.setThreadId(intent.getLongExtra("thread_id", 0))

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        messageList.layoutManager = layoutManager

        viewModel.state.observe(this)
    }

    override fun onChanged(state: MessageListViewState?) {
        when (state) {
            is MessageListViewState.ConversationLoaded -> onConversationLoaded(state)
            is MessageListViewState.ConversationError -> onConversationError(state)
            is MessageListViewState.MessagesLoaded -> onMessagesLoaded(state)
        }
    }

    private fun onConversationLoaded(conversationLoaded: MessageListViewState.ConversationLoaded) {
        title = conversationLoaded.data.getTitle()
    }

    private fun onConversationError(conversationError: MessageListViewState.ConversationError) {
        finish()
    }

    private fun onMessagesLoaded(messagesLoaded: MessageListViewState.MessagesLoaded) {
        messageList.adapter = MessageAdapter(this, messagesLoaded.data)
    }

}