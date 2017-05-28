package com.moez.QKSMS.ui.conversations

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.model.Conversation
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class ConversationAdapter(context: Context, data: OrderedRealmCollection<Conversation>?) :
        RealmRecyclerViewAdapter<Conversation, ConversationViewHolder>(context, data, true) {

    val mContext = context

    override fun onBindViewHolder(viewHolder: ConversationViewHolder, position: Int) {
        viewHolder.snippet.text = getItem(position)?.snippet
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ConversationViewHolder {
        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return ConversationViewHolder(layoutInflater.inflate(R.layout.conversation_list_item, parent, false))
    }
}