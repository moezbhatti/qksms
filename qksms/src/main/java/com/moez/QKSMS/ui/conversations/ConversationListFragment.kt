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

class ConversationListFragment : QkFragment<ConversationView, ConversationPresenter>(), ConversationView {

    var conversationList: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.conversations_fragment, container, false)

        conversationList = view.findViewById(R.id.conversation_list) as RecyclerView
        conversationList?.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun createPresenter(): ConversationPresenter = ConversationPresenter()

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