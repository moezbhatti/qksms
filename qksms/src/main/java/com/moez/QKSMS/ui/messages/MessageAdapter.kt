package com.moez.QKSMS.ui.messages

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.util.DateFormatter
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import javax.inject.Inject

class MessageAdapter(context: Context, data: OrderedRealmCollection<Message>?) :
        RealmRecyclerViewAdapter<Message, MessageViewHolder>(context, data, true) {
    val TAG = "MessageAdapter"

    val VIEWTYPE_IN = 0
    val VIEWTYPE_OUT = 1

    @Inject lateinit var dateFormatter: DateFormatter

    init {
        AppComponentManager.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessageViewHolder {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutRes = if (viewType == VIEWTYPE_IN) R.layout.message_list_item_in else R.layout.message_list_item_out
        return MessageViewHolder(layoutInflater.inflate(layoutRes, parent, false))
    }

    override fun onBindViewHolder(viewHolder: MessageViewHolder, position: Int) {
        val message = getItem(position)!!

        viewHolder.itemView.setOnClickListener {
            Log.i(TAG, message.toString())
        }

        viewHolder.body.text = message.body
        viewHolder.timestamp.text = dateFormatter.getMessageTimestamp(message.date)
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)!!
        return if (message.isMe()) VIEWTYPE_OUT else VIEWTYPE_IN
    }
}