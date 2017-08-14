package com.moez.QKSMS.ui.messages

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.model.Message
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class MessageAdapter(context: Context, data: OrderedRealmCollection<Message>?) :
        RealmRecyclerViewAdapter<Message, MessageViewHolder>(context, data, true) {
    val TAG = "MessageAdapter"

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessageViewHolder {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return MessageViewHolder(layoutInflater.inflate(R.layout.message_list_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: MessageViewHolder, position: Int) {
        val message = getItem(position)!!

        viewHolder.itemView.setOnClickListener {
            Log.i(TAG, message.toString())
        }

        viewHolder.body.text = message.body
        viewHolder.body.gravity = if (message.isMe()) Gravity.RIGHT else Gravity.LEFT
    }
}