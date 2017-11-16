package com.moez.QKSMS.presentation.messages

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.presentation.base.QkActivity
import io.realm.RealmResults
import kotlinx.android.synthetic.main.message_list_activity.*
import javax.inject.Inject

class MessagesActivity : QkActivity<MessagesViewModel, MessagesState>(), MessagesView {

    @Inject lateinit var themeManager: ThemeManager

    private lateinit var layoutManager: LinearLayoutManager

    override val viewModelClass = MessagesViewModel::class
    override val textChangedIntent by lazy { message.textChanges() }
    override val attachIntent by lazy { attach.clicks() }
    override val sendIntent by lazy { send.clicks() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppComponentManager.appComponent.inject(this)
        setContentView(R.layout.message_list_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.setView(this)

        layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        messageList.layoutManager = layoutManager
    }

    override fun render(state: MessagesState) {
        if (title != state.title) title = state.title
        if (messageList.adapter == null && state.messages?.isValid == true) messageList.adapter = createAdapter(state.messages)
        if (message.text.toString() != state.draft) message.setText(state.draft)
        if (state.hasError) finish()

        send.setTint(if (state.canSend) themeManager.color else resources.getColor(R.color.textTertiary))
        send.isEnabled = state.canSend
    }

    private fun createAdapter(messages: RealmResults<Message>): MessagesAdapter {
        val adapter = MessagesAdapter(messages)
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                viewModel.dataChanged()

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