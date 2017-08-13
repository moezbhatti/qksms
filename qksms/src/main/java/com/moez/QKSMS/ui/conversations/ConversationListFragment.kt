package com.moez.QKSMS.ui.conversations

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.moez.QKSMS.R
import com.moez.QKSMS.data.sync.ConversationSyncManager
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.ui.base.QkFragment
import io.realm.RealmResults

class ConversationListFragment : QkFragment<ConversationListView, ConversationListPresenter>(), ConversationListView {

    var conversationList: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.conversation_list_fragment, container, false)

        conversationList = view.findViewById(R.id.conversation_list)
        conversationList?.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun createPresenter(): ConversationListPresenter = ConversationListPresenter()

    override fun setConversations(conversations: RealmResults<Conversation>) {
        conversationList?.adapter = ConversationAdapter(context, conversations)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.conversations, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.load_conversations -> { // TODO: This is for testing, remove this
                ConversationSyncManager.copyToRealm(context)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

}