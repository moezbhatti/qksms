package com.moez.QKSMS.ui.conversations

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.ui.messages.MessageListActivity
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class ConversationAdapter(context: Context, data: OrderedRealmCollection<Conversation>?) :
        RealmRecyclerViewAdapter<Conversation, ConversationViewHolder>(context, data, true) {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ConversationViewHolder {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return ConversationViewHolder(layoutInflater.inflate(R.layout.conversation_list_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: ConversationViewHolder, position: Int) {
        val conversation = getItem(position)!!

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, MessageListActivity::class.java)
            intent.putExtra("thread_id", conversation.id)

            context.startActivity(intent)
        }

        viewHolder.avatar.contacts = conversation.contacts
        viewHolder.title.text = conversation.getTitle()
        viewHolder.snippet.text = conversation.snippet
    }
}