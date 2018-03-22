/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package presentation.feature.conversations

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.longClicks
import com.moez.QKSMS.R
import common.util.Colors
import common.util.DateFormatter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import model.Contact
import model.Conversation
import model.Message
import model.PhoneNumber
import presentation.common.Navigator
import presentation.common.base.FlowableAdapter
import presentation.common.base.QkViewHolder
import repository.MessageRepository
import javax.inject.Inject

class ConversationsAdapter @Inject constructor(
        val context: Context,
        val navigator: Navigator,
        val messageRepo: MessageRepository,
        val dateFormatter: DateFormatter,
        val colors: Colors
) : FlowableAdapter<Pair<Conversation, Message>>() {

    val clicks: Subject<Long> = PublishSubject.create()
    val longClicks: Subject<Long> = PublishSubject.create()

    private val disposables = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutRes = when (viewType) {
            0 -> R.layout.conversation_list_item
            else -> R.layout.conversation_list_item_unread
        }

        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(layoutRes, parent, false)

        if (viewType == 1) {
            disposables += colors.theme
                    .subscribe { color -> view.date.setTextColor(color) }
        }

        return QkViewHolder(view)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val conversation = getItem(position).first
        val message = getItem(position).second
        val view = viewHolder.itemView

        view.clicks().subscribe { clicks.onNext(conversation.id) }
        view.longClicks().subscribe { longClicks.onNext(conversation.id) }

        view.avatars.contacts = conversation.recipients.map { recipient ->
            recipient.contact ?: Contact().apply { numbers.add(PhoneNumber().apply { address = recipient.address }) }
        }
        view.title.text = conversation.getTitle()
        view.date.text = dateFormatter.getConversationTimestamp(message.date)
        view.snippet.text = if (message.isMe()) "You: ${message.getSummary()}" else message.getSummary()
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).second.read) 0 else 1
    }

    override fun areItemsTheSame(old: Pair<Conversation, Message>, new: Pair<Conversation, Message>): Boolean {
        return old.first.id == new.first.id
    }

    override fun areContentsTheSame(old: Pair<Conversation, Message>, new: Pair<Conversation, Message>): Boolean {
        return old.second.id == new.second.id &&
                old.second.read == new.second.read &&
                old.first.recipients == new.first.recipients
    }
}