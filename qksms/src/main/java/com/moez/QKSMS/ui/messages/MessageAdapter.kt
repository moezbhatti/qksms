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
import com.moez.QKSMS.util.ThemeManager
import com.moez.QKSMS.util.extensions.dpToPx
import com.moez.QKSMS.util.extensions.setBackgroundTint
import com.moez.QKSMS.util.extensions.setPadding
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessageAdapter(data: OrderedRealmCollection<Message>?) : RealmRecyclerViewAdapter<Message, MessageViewHolder>(data, true) {

    companion object {
        private val TIMESTAMP_THRESHOLD = 60

        private val VIEWTYPE_IN = 0
        private val VIEWTYPE_OUT = 1
    }

    @Inject lateinit var context: Context
    @Inject lateinit var themeManager: ThemeManager
    @Inject lateinit var dateFormatter: DateFormatter

    init {
        AppComponentManager.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessageViewHolder {
        val layoutRes: Int
        val bubbleColor: Int
        when (viewType) {
            VIEWTYPE_IN -> {
                layoutRes = R.layout.message_list_item_in
                bubbleColor = themeManager.bubbleColor
            }
            else -> {
                layoutRes = R.layout.message_list_item_out
                bubbleColor = themeManager.color
            }
        }

        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewHolder = MessageViewHolder(layoutInflater.inflate(layoutRes, parent, false))
        viewHolder.body.setBackgroundTint(bubbleColor)
        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: MessageViewHolder, position: Int) {
        val message = getItem(position)!!

        viewHolder.itemView.setOnClickListener { Timber.v(message.toString()) }

        bindGrouping(viewHolder, position)

        viewHolder.status?.visibility = when {
            message.isSending() || message.isFailedMessage() -> View.VISIBLE
            else -> View.GONE
        }
        viewHolder.status?.text = when {
            message.isSending() -> "Sending..."
            message.isFailedMessage() -> "Failed to send. Tap to try again"
            else -> null
        }

        viewHolder.avatar?.apply { contact = Contact() }
        viewHolder.body.text = message.body
        viewHolder.timestamp.text = dateFormatter.getMessageTimestamp(message.date)
    }

    private fun bindGrouping(viewHolder: MessageViewHolder, position: Int) {
        val message = getItem(position)!!
        val previous = if (position == 0) null else getItem(position - 1)
        val next = if (position == itemCount - 1) null else getItem(position + 1)

        val diff = TimeUnit.MILLISECONDS.toMinutes(message.date - (previous?.date ?: 0))
        viewHolder.timestamp.visibility = if (diff < TIMESTAMP_THRESHOLD) View.GONE else View.VISIBLE

        val sent = message.isMe()
        val canGroupWithPrevious = canGroup(message, previous)
        val canGroupWithNext = canGroup(message, next)

        when {
            !canGroupWithPrevious && canGroupWithNext -> {
                viewHolder.itemView.setPadding(bottom = 2.dpToPx(context))
                viewHolder.body.setBackgroundResource(if (sent) R.drawable.message_out_first else R.drawable.message_in_first)
                viewHolder.avatar?.visibility = View.INVISIBLE
            }
            canGroupWithPrevious && canGroupWithNext -> {
                viewHolder.itemView.setPadding(bottom = 2.dpToPx(context))
                viewHolder.body.setBackgroundResource(if (sent) R.drawable.message_out_middle else R.drawable.message_in_middle)
                viewHolder.avatar?.visibility = View.INVISIBLE
            }
            canGroupWithPrevious && !canGroupWithNext -> {
                viewHolder.itemView.setPadding(bottom = 16.dpToPx(context))
                viewHolder.body.setBackgroundResource(if (sent) R.drawable.message_out_last else R.drawable.message_in_last)
                viewHolder.avatar?.visibility = View.VISIBLE
            }
            else -> {
                viewHolder.itemView.setPadding(bottom = 16.dpToPx(context))
                viewHolder.body.setBackgroundResource(R.drawable.message_only)
                viewHolder.avatar?.visibility = View.VISIBLE
            }
        }
    }

    private fun canGroup(message: Message, other: Message?): Boolean {
        if (other == null) return false
        val diff = TimeUnit.MILLISECONDS.toMinutes(Math.abs(message.date - other.date))
        return message.isMe() == other.isMe() && diff < TIMESTAMP_THRESHOLD
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)!!
        return if (message.isMe()) VIEWTYPE_OUT else VIEWTYPE_IN
    }
}