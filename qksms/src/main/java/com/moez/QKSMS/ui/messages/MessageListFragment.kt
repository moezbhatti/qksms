package com.moez.QKSMS.ui.messages

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.moez.QKSMS.R
import com.moez.QKSMS.ui.base.QkFragment

class MessageListFragment : QkFragment() {

    private var threadId: Long = 0
    private var messageList: RecyclerView? = null
    private var viewModel: MessageListViewModel? = null

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
        viewModel = MessageListViewModel(threadId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.message_list_fragment, container, false)

        messageList = view.findViewById(R.id.message_list)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        messageList?.layoutManager = layoutManager
        messageList?.adapter = MessageAdapter(context, viewModel?.messages)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.messages, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }
    }

}