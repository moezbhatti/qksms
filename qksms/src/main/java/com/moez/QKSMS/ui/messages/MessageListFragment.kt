package com.moez.QKSMS.ui.messages

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.moez.QKSMS.R
import com.moez.QKSMS.ui.base.QkFragment
import javax.inject.Inject

class MessageListFragment : QkFragment() {

    @Inject lateinit var viewModel: MessageListViewModel

    private var messageList: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.message_list_fragment, container, false)

        messageList = view.findViewById(R.id.message_list)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        messageList?.layoutManager = layoutManager
        messageList?.adapter = MessageAdapter(context, viewModel.messages)

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