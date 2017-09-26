package com.moez.QKSMS.ui.messages

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.moez.QKSMS.R
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.ui.base.QkActivity
import com.moez.QKSMS.util.extensions.setTint
import io.realm.RealmResults
import kotlinx.android.synthetic.main.message_list_activity.*

class MessageListActivity : QkActivity(), Observer<MessageListViewState> {

    private lateinit var viewModel: MessageListViewModel

    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_list_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this)[MessageListViewModel::class.java]
        viewModel.threadId = intent.getLongExtra("threadId", 0)
        viewModel.state.observe(this, this)

        layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        messageList.layoutManager = layoutManager

        RxTextView.textChanges(message).subscribe { text -> viewModel.textChanged(text.toString()) }
        RxView.clicks(attach).subscribe { }
        RxView.clicks(send).subscribe { viewModel.sendMessage(message.text.toString()) }
    }

    override fun onChanged(state: MessageListViewState?) {
        state?.let {
            if (title != state.title) title = state.title
            if (messageList.adapter == null && state.messages?.isValid == true) messageList.adapter = createAdapter(state.messages)
            if (message.text.toString() != state.draft) message.setText(state.draft)
            if (state.hasError) finish()

            val color = if (state.canSend) R.color.colorPrimary else R.color.textTertiary
            send.setTint(resources.getColor(color))
            send.isEnabled = state.canSend
        }
    }

    private fun createAdapter(messages: RealmResults<Message>): MessageAdapter {
        val adapter = MessageAdapter(messages)
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart > 0) {
                    adapter.notifyItemChanged(positionStart - 1)
                }

                // If we're at the bottom, scroll down to show new messages
                val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                if (positionStart >= adapter.itemCount - 1 && lastVisiblePosition == positionStart - 1) {
                    messageList.scrollToPosition(positionStart)
                }
            }
        })
        return adapter
    }
}