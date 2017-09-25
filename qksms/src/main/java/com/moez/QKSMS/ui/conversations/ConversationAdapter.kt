package com.moez.QKSMS.ui.conversations

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.ui.messages.MessageListActivity
import com.moez.QKSMS.util.extensions.getColorCompat
import io.realm.OrderedRealmCollection
import io.realm.RealmList
import io.realm.RealmRecyclerViewAdapter
import javax.inject.Inject

class ConversationAdapter(data: OrderedRealmCollection<Message>?) : RealmRecyclerViewAdapter<Message, ConversationViewHolder>(data, true) {

    @Inject lateinit var context: Context
    @Inject lateinit var messageRepo: MessageRepository

    init {
        AppComponentManager.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ConversationViewHolder {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return ConversationViewHolder(layoutInflater.inflate(R.layout.conversation_list_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: ConversationViewHolder, position: Int) {
        val message = getItem(position)!!
        val conversation = messageRepo.getConversation(message.threadId)

        viewHolder.itemView.setOnClickListener {
            context.startActivity(Intent(context, MessageListActivity::class.java)
                    .putExtra("threadId", message.threadId))
        }

        viewHolder.avatar.contacts = conversation?.contacts ?: RealmList()
        viewHolder.title.text = conversation?.getTitle()
        viewHolder.snippet.text = message.body
        viewHolder.snippet.setTextColor(context.getColorCompat(if (message.read) R.color.textTertiary else R.color.textPrimary))
    }
}