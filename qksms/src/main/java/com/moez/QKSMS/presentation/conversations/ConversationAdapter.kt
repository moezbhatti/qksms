package com.moez.QKSMS.presentation.conversations

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.presentation.messages.MessageListActivity
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.common.util.extensions.getColorCompat
import io.realm.OrderedRealmCollection
import io.realm.RealmList
import io.realm.RealmRecyclerViewAdapter
import javax.inject.Inject

class ConversationAdapter(data: OrderedRealmCollection<Message>?) : RealmRecyclerViewAdapter<Message, ConversationViewHolder>(data, true) {

    @Inject lateinit var context: Context
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var dateFormatter: DateFormatter
    @Inject lateinit var themeManager: ThemeManager

    init {
        AppComponentManager.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ConversationViewHolder {
        val layoutRes = when(viewType) {
            0 -> R.layout.conversation_list_item
            else -> R.layout.conversation_list_item_unread
        }

        val layoutInflater = LayoutInflater.from(context)
        val viewHolder = ConversationViewHolder(layoutInflater.inflate(layoutRes, parent, false))

        if (viewType == 1) viewHolder.date.setTextColor(themeManager.color)

        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: ConversationViewHolder, position: Int) {
        val message = getItem(position)!!
        val conversation = messageRepo.getConversation(message.threadId)

        viewHolder.itemView.setOnClickListener {
            context.startActivity(Intent(context, MessageListActivity::class.java)
                    .putExtra("threadId", message.threadId))
        }

        val dateColor = if (message.read) context.getColorCompat(R.color.textTertiary) else themeManager.color
        val snippetColor = context.getColorCompat(if (message.read) R.color.textTertiary else R.color.textPrimary)

        viewHolder.avatar.contacts = conversation?.contacts ?: RealmList()
        viewHolder.title.text = conversation?.getTitle()

        viewHolder.date.text = dateFormatter.getConversationTimestamp(message.date)
        viewHolder.date.setTextColor(dateColor)

        viewHolder.snippet.text = message.body
        viewHolder.snippet.setTextColor(snippetColor)
        viewHolder.snippet.maxLines = if (message.read) 1 else 3
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)!!.read) 0 else 1
    }
}