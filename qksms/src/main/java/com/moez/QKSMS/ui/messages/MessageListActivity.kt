package com.moez.QKSMS.ui.messages

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.moez.QKSMS.R
import com.moez.QKSMS.ui.base.QkActivity
import com.moez.QKSMS.util.extensions.setTint
import kotlinx.android.synthetic.main.message_list_activity.*

class MessageListActivity : QkActivity(), Observer<MessageListViewState> {

    private lateinit var viewModel: MessageListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_list_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this)[MessageListViewModel::class.java]
        viewModel.threadId = intent.getLongExtra("thread_id", 0)
        viewModel.state.observe(this, this)

        messageList.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }

        RxTextView.textChanges(message).subscribe { text -> viewModel.textChanged(text.toString()) }
        RxView.clicks(attach).subscribe { }
        RxView.clicks(send).subscribe { viewModel.sendMessage(message.text.toString()) }
    }

    override fun onChanged(state: MessageListViewState?) {
        state?.let {
            if (title != state.title) title = state.title
            if (messageList.adapter == null && state.messages?.isValid == true) messageList.adapter = MessageAdapter(this, state.messages)
            if (message.text.toString() != state.draft) message.setText(state.draft)
            if (state.hasError) finish()

            val color = if (state.canSend) R.color.colorPrimary else R.color.textTertiary
            send.setTint(resources.getColor(color))
            send.isEnabled = state.canSend
        }
    }
}