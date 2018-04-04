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
package feature.conversations

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.longClicks
import com.moez.QKSMS.R
import common.base.FlowableAdapter
import common.base.QkViewHolder
import common.util.Colors
import common.util.DateFormatter
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import model.Contact
import model.Conversation
import model.PhoneNumber
import javax.inject.Inject

class ConversationsAdapter @Inject constructor(
        private val context: Context,
        private val dateFormatter: DateFormatter,
        private val colors: Colors
) : FlowableAdapter<Conversation>() {

    val clicks: Subject<Long> = PublishSubject.create()
    val longClicks: Subject<Long> = PublishSubject.create()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.conversation_list_item, parent, false)

        if (viewType == 1) {
            view.title.setTypeface(view.title.typeface, Typeface.BOLD)

            view.snippet.setTypeface(view.snippet.typeface, Typeface.BOLD)
            view.snippet.textColorObservable = colors.textPrimary
            view.snippet.maxLines = 5

            view.date.setTypeface(view.date.typeface, Typeface.BOLD)
            view.date.textColorObservable = colors.textPrimary
        }

        return QkViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val conversation = getItem(position)
        val view = viewHolder.itemView

        view.clicks().subscribe { clicks.onNext(conversation.id) }
        view.longClicks().subscribe { longClicks.onNext(conversation.id) }

        view.avatars.contacts = conversation.recipients.map { recipient ->
            recipient.contact ?: Contact().apply { numbers.add(PhoneNumber().apply { address = recipient.address }) }
        }
        view.title.text = conversation.getTitle()
        view.date.text = dateFormatter.getConversationTimestamp(conversation.date)
        view.snippet.text = if (conversation.me) "You: ${conversation.snippet}" else conversation.snippet
    }

    override fun getItemId(index: Int): Long {
        return getItem(index).id
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).read) 0 else 1
    }

    override fun areItemsTheSame(old: Conversation, new: Conversation): Boolean {
        return old.id == new.id
    }
}