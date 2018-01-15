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
package presentation.feature.compose

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.moez.QKSMS.R
import common.util.Colors
import common.util.DateFormatter
import common.util.extensions.dpToPx
import common.util.extensions.setBackgroundTint
import common.util.extensions.setPadding
import common.util.extensions.setVisible
import data.model.Contact
import data.model.Message
import data.model.PhoneNumber
import interactor.LoadMmsParts
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.message_list_item_in.view.*
import presentation.common.base.QkViewHolder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessagesAdapter @Inject constructor(
        private val context: Context,
        private val colors: Colors,
        private val dateFormatter: DateFormatter,
        private val loadMmsParts: LoadMmsParts)
    : RealmRecyclerViewAdapter<Message, QkViewHolder>(null, true) {

    companion object {
        private val VIEWTYPE_ME = 1
        private val TIMESTAMP_THRESHOLD = 10
    }

    val longClicks: Subject<Message> = PublishSubject.create<Message>()

    private val people = ArrayList<String>()
    private val selected = ArrayList<Long>()
    private val disposables = CompositeDisposable()

    /**
     * If the viewType is negative, then the viewHolder has an attachment. We'll consider
     * this a unique viewType even though it uses the same view, so that regular messages
     * don't need clipToOutline set to true, and they don't need to worry about images
     */
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): QkViewHolder {

        val layoutInflater = LayoutInflater.from(context)
        val view: View

        val hasThumbnail = viewType < 0
        val absViewType = Math.abs(viewType)

        when (absViewType) {
            VIEWTYPE_ME -> {
                view = layoutInflater.inflate(R.layout.message_list_item_out, parent, false)
                disposables += colors.bubble
                        .subscribe { color -> view.messageBackground.setBackgroundTint(color) }
            }
            else -> {
                view = layoutInflater.inflate(R.layout.message_list_item_in, parent, false)
                disposables += colors.theme
                        .subscribe { color -> view.messageBackground.setBackgroundTint(color) }
            }
        }

        if (hasThumbnail) {
            view.messageBackground.clipToOutline = true
        }

        if (absViewType != VIEWTYPE_ME) {
            view.avatar.contact = Contact().apply {
                numbers.add(PhoneNumber().apply { address = people[absViewType - 2] })
            }
        }

        return QkViewHolder(view)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        disposables += loadMmsParts
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val message = getItem(position)!!
        val view = viewHolder.itemView

        RxView.clicks(view).subscribe {
            if (selected.contains(message.id)) selected.remove(message.id)
            else selected.add(message.id)
            notifyItemChanged(position)
        }
        RxView.longClicks(view).subscribe { longClicks.onNext(message) }

        view.subject.text = message.subject
        view.subject.setVisible(view.subject.text.isNotBlank())

        view.body.text = message.getText()
        view.body.setVisible(message.isSms() || view.body.text.isNotEmpty() || message.parts.all { it.image == null })

        view.timestamp.text = dateFormatter.getMessageTimestamp(message.date)

        bindMmsPreview(view, position)
        bindStatus(view, position)
        bindGrouping(view, position)
    }

    private fun bindMmsPreview(view: View, position: Int) {
        val message = getItem(position)!!

        // If it's an MMS and the parts haven't been loaded, kick off a load
        if (message.isMms() && message.parts.isEmpty()) {
            loadMmsParts.execute(message.id)
        }

        view.mmsPreview.parts = message.parts
    }

    private fun bindStatus(view: View, position: Int) {
        val message = getItem(position)!!
        val age = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - message.date)
        val timestamp = dateFormatter.getTimestamp(message.date)

        view.status.text = when {
            message.isSending() -> context.getString(R.string.message_status_sending)
            message.isDelivered() -> context.getString(R.string.message_status_delivered, timestamp)
            message.isFailedMessage() -> context.getString(R.string.message_status_failed)
            else -> timestamp
        }

        view.status.visibility = when {
            selected.contains(message.id) -> View.VISIBLE
            message.isSending() -> View.VISIBLE
            message.isDelivered() && age <= TIMESTAMP_THRESHOLD -> View.VISIBLE
            message.isFailedMessage() -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun bindGrouping(view: View, position: Int) {
        val message = getItem(position)!!
        val previous = if (position == 0) null else getItem(position - 1)
        val next = if (position == itemCount - 1) null else getItem(position + 1)

        val diff = TimeUnit.MILLISECONDS.toMinutes(message.date - (previous?.date ?: 0))

        val sent = message.isMe()
        val canGroupWithPrevious = canGroup(message, previous)
        val canGroupWithNext = canGroup(message, next)

        view.timestamp.visibility = if (diff < TIMESTAMP_THRESHOLD) View.GONE else View.VISIBLE

        when {
            !canGroupWithPrevious && canGroupWithNext -> {
                view.setPadding(bottom = 2.dpToPx(context))
                view.messageBackground.setBackgroundResource(if (sent) R.drawable.message_out_first else R.drawable.message_in_first)
                view.avatar?.visibility = View.INVISIBLE
            }
            canGroupWithPrevious && canGroupWithNext -> {
                view.setPadding(bottom = 2.dpToPx(context))
                view.messageBackground.setBackgroundResource(if (sent) R.drawable.message_out_middle else R.drawable.message_in_middle)
                view.avatar?.visibility = View.INVISIBLE
            }
            canGroupWithPrevious && !canGroupWithNext -> {
                view.setPadding(bottom = 16.dpToPx(context))
                view.messageBackground.setBackgroundResource(if (sent) R.drawable.message_out_last else R.drawable.message_in_last)
                view.avatar?.visibility = View.VISIBLE
            }
            else -> {
                view.setPadding(bottom = 16.dpToPx(context))
                view.messageBackground.setBackgroundResource(R.drawable.message_only)
                view.avatar?.visibility = View.VISIBLE
            }
        }

        if (getItemViewType(position) < 0) view.messageBackground.setBackgroundResource(R.drawable.message_only)
    }

    private fun canGroup(message: Message, other: Message?): Boolean {
        if (other == null) return false
        val samePerson = message.isMe() && other.isMe() || (!message.isMe() && !other.isMe() && message.address == other.address)
        val diff = TimeUnit.MILLISECONDS.toMinutes(Math.abs(message.date - other.date))
        return samePerson && diff < TIMESTAMP_THRESHOLD
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)!!

        var index = if (message.isMe()) {
            VIEWTYPE_ME
        } else {
            if (!people.contains(message.address)) {
                people.add(message.address)
            }
            2 + people.indexOf(message.address)
        }

        // If it contains a thumbnail, then use the negative viewtype
        if (message.parts.filter { it.isImage() }.any()) {
            index *= -1
        }

        return index
    }
}