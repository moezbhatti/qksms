package com.moez.QKSMS.ui.messages

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.util.DateFormatter
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import timber.log.Timber
import javax.inject.Inject

class MessageAdapter(context: Context, data: OrderedRealmCollection<Message>?) :
        RealmRecyclerViewAdapter<Message, MessageViewHolder>(context, data, true) {

    companion object {
        private val VIEWTYPE_IN = 0
        private val VIEWTYPE_OUT = 1
    }

    @Inject lateinit var dateFormatter: DateFormatter

    init {
        AppComponentManager.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessageViewHolder {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutRes = when (viewType) {
            VIEWTYPE_IN -> R.layout.message_list_item_in
            else -> R.layout.message_list_item_out
        }
        return MessageViewHolder(layoutInflater.inflate(layoutRes, parent, false))
    }

    override fun onBindViewHolder(viewHolder: MessageViewHolder, position: Int) {
        val message = getItem(position)!!

        viewHolder.itemView.setOnClickListener { Timber.v(message.toString()) }
        
        bindGrouping(viewHolder, position)

        viewHolder.avatar.contact = Contact()
        viewHolder.body.text = message.body
        viewHolder.timestamp.text = dateFormatter.getMessageTimestamp(message.date)
    }
    
    private fun bindGrouping(viewHolder: MessageViewHolder, position: Int) {

        val message = getItem(position)!!
        val previous = if (position == 0) null else getItem(position - 1)
        val next = if (position == itemCount - 1) null else getItem(position + 1)

        val sent = message.isMe()
        if (previous != null && next != null) { // Mid message
            if (previous.isMe() == sent && sent == next.isMe()) { // Mid in group
                viewHolder.body.setBackgroundResource(if (sent) R.drawable.message_out_middle else R.drawable.message_in_middle)
                viewHolder.avatar.visibility = View.INVISIBLE
            } else if (previous.isMe() == sent && sent != next.isMe()) { // Last in group
                viewHolder.body.setBackgroundResource(if (sent) R.drawable.message_out_last else R.drawable.message_in_last)
                viewHolder.avatar.visibility = View.VISIBLE
            } else if (previous.isMe() != sent && sent == next.isMe()) { // First in group
                viewHolder.body.setBackgroundResource(if (sent) R.drawable.message_out_first else R.drawable.message_in_first)
                viewHolder.avatar.visibility = View.INVISIBLE
            } else { // Only in group
                viewHolder.body.setBackgroundResource(R.drawable.message_only)
                viewHolder.avatar.visibility = View.VISIBLE
            }
        } else if (previous != null && next == null) { // Last message
            if (previous.isMe() == sent) { // Last in group
                viewHolder.body.setBackgroundResource(if (sent) R.drawable.message_out_last else R.drawable.message_in_last)
                viewHolder.avatar.visibility = View.VISIBLE
            } else { // Only in group
                viewHolder.body.setBackgroundResource(R.drawable.message_only)
                viewHolder.avatar.visibility = View.VISIBLE
            }
        } else if (previous == null && next != null) { // First message
            if (sent == next.isMe()) { // First in group
                viewHolder.body.setBackgroundResource(if (sent) R.drawable.message_out_first else R.drawable.message_in_first)
                viewHolder.avatar.visibility = View.INVISIBLE
            } else { // Only in group
                viewHolder.body.setBackgroundResource(R.drawable.message_only)
                viewHolder.avatar.visibility = View.VISIBLE
            }
        } else { // Only message
            viewHolder.body.setBackgroundResource(R.drawable.message_only)
            viewHolder.avatar.visibility = View.VISIBLE
        }
        
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)!!
        return if (message.isMe()) VIEWTYPE_OUT else VIEWTYPE_IN
    }
}