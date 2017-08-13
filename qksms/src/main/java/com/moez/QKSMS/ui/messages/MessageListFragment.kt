package com.moez.QKSMS.ui.messages

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.moez.QKSMS.R
import com.moez.QKSMS.data.sync.MessageSyncManager
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.ui.base.QkFragment
import io.realm.RealmResults

class MessageListFragment : QkFragment<MessageListView, MessageListPresenter>(), MessageListView {

    var threadId: Long = 0
    var messageList: RecyclerView? = null

    companion object {
        fun newInstance(threadId: Long): MessageListFragment {
            val myFragment = MessageListFragment()

            val args = Bundle()
            args.putLong("thread_id", threadId)
            myFragment.arguments = args

            return myFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        threadId = arguments.getLong("thread_id", 0)
        presenter?.threadId = threadId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.message_list_fragment, container, false)

        messageList = view.findViewById(R.id.message_list)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        messageList?.layoutManager = layoutManager

        return view
    }

    override fun createPresenter(): MessageListPresenter {
        val presenter = MessageListPresenter()

        if (threadId != 0L) {
            presenter.threadId = threadId
        }

        return presenter
    }

    override fun setMessages(messages: RealmResults<Message>) {
        messageList?.adapter = MessageAdapter(context, messages)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.messages, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.load_messages -> { // TODO: This is for testing, remove this
                MessageSyncManager.copyToRealm(context, threadId)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

}