package com.moez.QKSMS.presentation.main

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.data.model.ConversationMessagePair
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.base.FlowableAdapter
import com.moez.QKSMS.presentation.base.QkViewHolder
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import javax.inject.Inject

class ConversationsAdapter(flowable: Flowable<List<ConversationMessagePair>>) : FlowableAdapter<ConversationMessagePair>(flowable) {

    @Inject lateinit var context: Context
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var dateFormatter: DateFormatter
    @Inject lateinit var themeManager: ThemeManager

    private val disposables = CompositeDisposable()

    init {
        appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): QkViewHolder {
        val layoutRes = when (viewType) {
            0 -> R.layout.conversation_list_item
            else -> R.layout.conversation_list_item_unread
        }

        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(layoutRes, parent, false)

        if (viewType == 1) {
            disposables += themeManager.color
                    .subscribe { color -> view.date.setTextColor(color) }
        }

        return QkViewHolder(view)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val conversation = getItem(position).conversation
        val message = getItem(position).message
        val view = viewHolder.itemView

        RxView.clicks(view).subscribe { navigator.showConversation(message.threadId) }

        view.avatars.contacts = conversation.contacts
        view.title.text = conversation.getTitle()
        view.date.text = dateFormatter.getConversationTimestamp(message.date)
        view.snippet.text = message.body
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).message.read) 0 else 1
    }

    override fun areItemsTheSame(old: ConversationMessagePair, new: ConversationMessagePair): Boolean {
        return old.conversation.id == new.conversation.id
    }
}