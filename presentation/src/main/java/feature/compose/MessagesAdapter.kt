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
package feature.compose

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.moez.QKSMS.R
import common.base.QkViewHolder
import common.util.Colors
import common.util.DateFormatter
import common.util.extensions.dpToPx
import common.util.extensions.setBackgroundTint
import common.util.extensions.setPadding
import common.util.extensions.setVisible
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import kotlinx.android.synthetic.main.message_list_item_in.view.*
import model.Conversation
import model.Message
import model.Recipient
import util.extensions.isImage
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessagesAdapter @Inject constructor(
        private val context: Context,
        private val colors: Colors,
        private val dateFormatter: DateFormatter
) : RealmRecyclerViewAdapter<Message, QkViewHolder>(null, true) {

    companion object {
        private const val VIEW_TYPE_MESSAGE_IN = 0
        private const val VIEW_TYPE_MESSAGE_OUT = 1
        private const val TIMESTAMP_THRESHOLD = 10
    }

    val clicks: Subject<Message> = PublishSubject.create<Message>()
    val longClicks: Subject<Message> = PublishSubject.create<Message>()

    var data: Pair<Conversation, RealmResults<Message>>? = null
        set(value) {
            if (field === value) return

            field = value
            contactCache.clear()

            // Update the theme
            val threadId = value?.first?.id ?: 0
            theme = colors.themeForConversation(threadId)
            textPrimaryOnTheme = colors.textPrimaryOnThemeForConversation(threadId)

            updateData(value?.second)
        }

    /**
     * Safely return the conversation, if available
     */
    private val conversation: Conversation?
        get() = data?.first?.takeIf { it.isValid }

    private val contactCache = ContactCache()
    private val selected = HashMap<Long, Boolean>()
    private val disposables = CompositeDisposable()

    private var theme = colors.theme
    private var textPrimaryOnTheme = colors.textPrimaryOnTheme

    /**
     * If the viewType is negative, then the viewHolder has an attachment. We'll consider
     * this a unique viewType even though it uses the same view, so that regular messages
     * don't need clipToOutline set to true, and they don't need to worry about images
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {

        val layoutInflater = LayoutInflater.from(context)
        val view: View

        if (viewType == VIEW_TYPE_MESSAGE_OUT) {
            view = layoutInflater.inflate(R.layout.message_list_item_out, parent, false)
            disposables += colors.bubble
                    .subscribe { color -> view.messageBackground.setBackgroundTint(color) }
        } else {
            view = layoutInflater.inflate(R.layout.message_list_item_in, parent, false)
            view.avatar.threadId = conversation?.id ?: 0
            view.subject.textColorObservable = textPrimaryOnTheme
            view.body.textColorObservable = textPrimaryOnTheme
            disposables += theme
                    .subscribe { color -> view.messageBackground.setBackgroundTint(color) }
        }

        return QkViewHolder(view)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val message = getItem(position)!!
        val view = viewHolder.itemView

        RxView.clicks(view).subscribe {
            clicks.onNext(message)
            selected[message.id] = view.status.visibility != View.VISIBLE
            notifyItemChanged(position)
        }
        RxView.longClicks(view).subscribe { longClicks.onNext(message) }

        view.mmsPreview.parts = message.parts

        view.subject.text = message.getCleansedSubject()
        view.subject.setVisible(view.subject.text.isNotBlank())

        view.body.text = message.getText()
        view.body.setVisible(message.isSms() || view.body.text.isNotEmpty() || message.parts.all { it.image == null })

        view.timestamp.text = dateFormatter.getMessageTimestamp(message.date)

        bindAvatar(view, position)
        bindStatus(view, position)
        bindGrouping(view, position)
    }

    private fun bindAvatar(view: View, position: Int) {
        val message = getItem(position)!!
        if (message.isMe()) return

        view.avatar.threadId = conversation?.id ?: 0
        view.avatar.setContact(contactCache[message.address])
    }

    private fun bindStatus(view: View, position: Int) {
        val message = getItem(position)!!
        val next = if (position == itemCount - 1) null else getItem(position + 1)

        val age = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - message.date)
        val timestamp = dateFormatter.getTimestamp(message.date)

        view.status.text = when {
            message.isSending() -> context.getString(R.string.message_status_sending)
            message.isDelivered() -> context.getString(R.string.message_status_delivered, timestamp)
            message.isFailedMessage() -> context.getString(R.string.message_status_failed)
            !message.isMe() && conversation?.recipients?.size ?: 0 > 1 -> "${contactCache[message.address]?.getDisplayName()} • $timestamp"
            else -> timestamp
        }

        view.status.setVisible(when {
            selected[message.id] == true -> true
            message.isSending() -> true
            message.isFailedMessage() -> true
            selected[message.id] == false -> false
            conversation?.recipients?.size ?: 0 > 1 && !message.isMe() && next?.compareSender(message) != true -> true
            message.isDelivered() && next?.isDelivered() != true && age <= TIMESTAMP_THRESHOLD -> true
            else -> false
        })
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

        // If we're showing any thumbnails, set clipToOutline to true
        val hasImages = message.parts.filter { it.isImage() }.any()
        view.messageBackground.clipToOutline = hasImages

        // setClipToOutline doesn't work unless all of the corners have the same radius, so we have
        // to use the message_only bubble
        if (hasImages) view.messageBackground.setBackgroundResource(R.drawable.message_only)
    }

    private fun canGroup(message: Message, other: Message?): Boolean {
        if (other == null) return false
        val diff = TimeUnit.MILLISECONDS.toMinutes(Math.abs(message.date - other.date))
        return message.compareSender(other) && diff < TIMESTAMP_THRESHOLD
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)!!
        return when (message.isMe()) {
            true -> VIEW_TYPE_MESSAGE_OUT
            false -> VIEW_TYPE_MESSAGE_IN
        }
    }

    /**
     * Cache the contacts in a map by the address, because the messages we're binding don't have
     * a reference to the contact.
     */
    private inner class ContactCache : HashMap<String, Recipient?>() {

        override fun get(key: String): Recipient? {
            if (super.get(key)?.isValid != true) {
                set(key, conversation?.recipients?.firstOrNull { PhoneNumberUtils.compare(it.address, key) })
            }

            return super.get(key)?.takeIf { it.isValid }
        }

    }
}