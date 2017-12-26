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
package com.moez.QKSMS.presentation.compose

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setPadding
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.model.PhoneNumber
import com.moez.QKSMS.presentation.common.base.QkViewHolder
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.message_list_item_in.view.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessagesAdapter @Inject constructor(
        private val context: Context,
        private val colors: Colors,
        private val dateFormatter: DateFormatter)
    : RealmRecyclerViewAdapter<Message, QkViewHolder>(null, true) {

    companion object {
        private val VIEWTYPE_ME = -1
        private val TIMESTAMP_THRESHOLD = 10
    }

    val longClicks: Subject<Message> = PublishSubject.create<Message>()

    private val people = ArrayList<String>()
    private val disposables = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): QkViewHolder {

        val layoutInflater = LayoutInflater.from(context)
        val view: View

        when (viewType) {
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

        if (viewType != VIEWTYPE_ME) {
            view.avatar.contact = Contact().apply {
                numbers.add(PhoneNumber().apply { address = people[viewType] })
            }
        }

        return QkViewHolder(view)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val message = getItem(position)!!
        val view = viewHolder.itemView

        RxView.clicks(view).subscribe { Timber.v(message.toString()) }
        RxView.longClicks(view).subscribe { longClicks.onNext(message) }

        bindGrouping(view, position)

        val age = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - message.date)

        view.status?.text = when {
            message.isSending() -> context.getString(R.string.message_status_sending)
            message.isDelivered() && age <= TIMESTAMP_THRESHOLD -> context.getString(R.string.message_status_delivered)
            message.isFailedMessage() -> context.getString(R.string.message_status_failed)
            else -> null
        }
        view.status?.setVisible(!view.status?.text.isNullOrBlank())

        view.body.text = message.body
        view.timestamp.text = dateFormatter.getMessageTimestamp(message.date)
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
    }

    private fun canGroup(message: Message, other: Message?): Boolean {
        if (other == null) return false
        val diff = TimeUnit.MILLISECONDS.toMinutes(Math.abs(message.date - other.date))
        return message.isMe() == other.isMe() && diff < TIMESTAMP_THRESHOLD
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)!!
        if (message.isMe()) {
            return VIEWTYPE_ME
        }

        if (!people.contains(message.address)) {
            people.add(message.address)
        }
        return people.indexOf(message.address)
    }
}