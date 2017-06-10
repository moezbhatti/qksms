package com.moez.QKSMS.ui.conversations

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.ui.base.QkFragment
import io.realm.RealmResults

class ConversationFragment : QkFragment<ConversationView, ConversationPresenter>(), ConversationView {

    var conversationList: RecyclerView? = null

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

}