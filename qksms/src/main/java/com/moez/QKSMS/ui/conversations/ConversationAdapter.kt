package com.moez.QKSMS.ui.conversations

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.ui.messages.MessageListActivity
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class ConversationAdapter(context: Context, data: OrderedRealmCollection<Conversation>) :
        RealmRecyclerViewAdapter<Conversation, ConversationViewHolder>(context, data, true) {

    val mContext = context

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ConversationViewHolder {
        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return ConversationViewHolder(layoutInflater.inflate(R.layout.conversation_list_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: ConversationViewHolder, position: Int) {
        val conversation = getItem(position)

        viewHolder.snippet.text = conversation?.snippet
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(mContext, MessageListActivity::class.java)
            intent.putExtra("thread_id", conversation?.id)

            mContext.startActivity(intent)
        }
    }
}