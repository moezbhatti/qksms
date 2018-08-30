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
package com.moez.QKSMS.feature.compose

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Typeface
import android.telephony.PhoneNumberUtils
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.base.QkRealmAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.common.util.extensions.forwardTouches
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setPadding
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.compat.SubscriptionManagerCompat
import com.moez.QKSMS.extensions.isImage
import com.moez.QKSMS.extensions.isVCard
import com.moez.QKSMS.extensions.isVideo
import com.moez.QKSMS.feature.compose.BubbleUtils.canGroup
import com.moez.QKSMS.feature.compose.BubbleUtils.getBubble
import com.moez.QKSMS.feature.compose.part.PartsAdapter
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.Recipient
import com.moez.QKSMS.util.Preferences
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.message_list_item_in.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessagesAdapter @Inject constructor(
        private val context: Context,
        private val colors: Colors,
        private val dateFormatter: DateFormatter,
        private val navigator: Navigator,
        private val prefs: Preferences,
        private val subscriptionManager: SubscriptionManagerCompat
) : QkRealmAdapter<Message>() {

    companion object {
        private const val VIEW_TYPE_MESSAGE_IN = 0
        private const val VIEW_TYPE_MESSAGE_OUT = 1
    }

    val clicks: Subject<Message> = PublishSubject.create<Message>()
    val cancelSending: Subject<Message> = PublishSubject.create<Message>()

    var data: Pair<Conversation, RealmResults<Message>>? = null
        set(value) {
            if (field === value) return

            field = value
            contactCache.clear()

            // Update the theme
            theme = colors.theme(value?.first?.id ?: 0)

            updateData(value?.second)
        }

    /**
     * Safely return the conversation, if available
     */
    private val conversation: Conversation?
        get() = data?.first?.takeIf { it.isValid }

    /**
     * Mark this message as highlighted
     */
    var highlight: Long = -1L
        set(value) {
            if (field == value) return

            field = value
            notifyDataSetChanged()
        }

    private val contactCache = ContactCache()
    private val expanded = HashMap<Long, Boolean>()
    private val partsViewPool = RecyclerView.RecycledViewPool()
    private val subs = subscriptionManager.activeSubscriptionInfoList

    var theme: Colors.Theme = colors.theme()

    /**
     * If the viewType is negative, then the viewHolder has an attachment. We'll consider
     * this a unique viewType even though it uses the same view, so that regular messages
     * don't need clipToOutline set to true, and they don't need to worry about images
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {

        // Use the parent's context to inflate the layout, otherwise link clicks will crash the app
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View

        if (viewType == VIEW_TYPE_MESSAGE_OUT) {
            view = layoutInflater.inflate(R.layout.message_list_item_out, parent, false)
            view.findViewById<ImageView>(R.id.cancelIcon).setTint(theme.theme)
            view.findViewById<ProgressBar>(R.id.cancel).setTint(theme.theme)
        } else {
            view = layoutInflater.inflate(R.layout.message_list_item_in, parent, false)
            view.avatar.threadId = conversation?.id ?: 0
            view.body.setTextColor(theme.textPrimary)
            view.body.setBackgroundTint(theme.theme)
        }

        view.attachments.adapter = PartsAdapter(context, navigator, theme)
        view.attachments.setRecycledViewPool(partsViewPool)
        view.body.forwardTouches(view)

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val message = getItem(adapterPosition)!!
                when (toggleSelection(message.id, false)) {
                    true -> view.isActivated = isSelected(message.id)
                    false -> {
                        clicks.onNext(message)
                        expanded[message.id] = view.status.visibility != View.VISIBLE
                        notifyItemChanged(adapterPosition)
                    }
                }
            }
            view.setOnLongClickListener {
                val message = getItem(adapterPosition)!!
                toggleSelection(message.id)
                view.isActivated = isSelected(message.id)
                true
            }
        }
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val message = getItem(position)!!
        val previous = if (position == 0) null else getItem(position - 1)
        val next = if (position == itemCount - 1) null else getItem(position + 1)
        val view = viewHolder.itemView


        // Update the selected state
        view.isActivated = isSelected(message.id) || highlight == message.id


        // Bind the cancel view
        view.findViewById<ProgressBar>(R.id.cancel)?.let { cancel ->
            val isCancellable = message.isSending() && message.date > System.currentTimeMillis()
            cancel.setVisible(isCancellable)
            cancel.clicks().subscribe { cancelSending.onNext(message) }
            cancel.progress = 2

            if (isCancellable) {
                val delay = when (prefs.sendDelay.get()) {
                    Preferences.SEND_DELAY_SHORT -> 3000
                    Preferences.SEND_DELAY_MEDIUM -> 5000
                    Preferences.SEND_DELAY_LONG -> 10000
                    else -> 0
                }
                val progress = (1 - (message.date - System.currentTimeMillis()) / delay.toFloat()) * 100

                ObjectAnimator.ofInt(cancel, "progress", progress.toInt(), 100)
                        .setDuration(message.date - System.currentTimeMillis())
                        .start()
            }
        }


        // Bind the message status
        bindStatus(viewHolder, message, next)


        // Bind the timestamp
        val timeSincePrevious = TimeUnit.MILLISECONDS.toMinutes(message.date - (previous?.date ?: 0))
        val simIndex = subs.takeIf { it.size > 1 }?.indexOfFirst { it.subscriptionId == message.subId } ?: -1

        view.timestamp.text = dateFormatter.getMessageTimestamp(message.date)
        view.simIndex.text = "${simIndex + 1}"

        view.timestamp.setVisible(timeSincePrevious >= BubbleUtils.TIMESTAMP_THRESHOLD || message.subId != previous?.subId && simIndex != -1)
        view.sim.setVisible(message.subId != previous?.subId && simIndex != -1)
        view.simIndex.setVisible(message.subId != previous?.subId && simIndex != -1)


        // Bind the grouping
        val media = message.parts.filter { it.isImage() || it.isVideo() || it.isVCard() }
        view.setPadding(bottom = if (canGroup(message, next)) 0 else 16.dpToPx(context))


        // Bind the avatar
        if (!message.isMe()) {
            view.avatar.threadId = conversation?.id ?: 0
            view.avatar.setContact(contactCache[message.address])
            view.avatar.setVisible(!canGroup(message, next), View.INVISIBLE)
        }


        // Bind the body text
        view.body.text = when (message.isSms()) {
            true -> message.body
            false -> {
                val subject = message.getCleansedSubject()
                val body = message.parts
                        .filter { part -> !part.isVCard() }
                        .mapNotNull { part -> part.text }
                        .filter { part -> part.isNotBlank() }
                        .joinToString("\n")

                when {
                    subject.isNotBlank() -> {
                        val spannable = SpannableString(if (body.isNotBlank()) "$subject\n$body" else subject)
                        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, subject.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                        spannable
                    }
                    else -> body
                }
            }
        }
        view.body.setVisible(message.isSms() || view.body.text.isNotBlank())
        view.body.setBackgroundResource(getBubble(
                canGroupWithPrevious = canGroup(message, previous) || media.isNotEmpty(),
                canGroupWithNext = canGroup(message, next),
                isMe = message.isMe()))


        // Bind the attachments
        val partsAdapter = view.attachments.adapter as PartsAdapter
        partsAdapter.setData(message, previous, next, view)
    }

    private fun bindStatus(viewHolder: QkViewHolder, message: Message, next: Message?) {
        val view = viewHolder.itemView

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
            expanded[message.id] == true -> true
            message.isSending() -> true
            message.isFailedMessage() -> true
            expanded[message.id] == false -> false
            conversation?.recipients?.size ?: 0 > 1 && !message.isMe() && next?.compareSender(message) != true -> true
            message.isDelivered() && next?.isDelivered() != true && age <= BubbleUtils.TIMESTAMP_THRESHOLD -> true
            else -> false
        })
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