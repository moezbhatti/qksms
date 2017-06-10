package com.moez.QKSMS.ui.conversations

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.model.Conversation
import io.realm.Realm

class ConversationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.conversations_fragment, container, false)

        val realmResults = Realm.getDefaultInstance().where(Conversation::class.java).findAll()

        val conversations = view.findViewById(R.id.conversation_list) as RecyclerView
        conversations.layoutManager = LinearLayoutManager(context)
        conversations.adapter = ConversationAdapter(context, realmResults)

        return view
    }

}