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
import android.os.Build
import android.text.Layout
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
import com.moez.QKSMS.common.base.QkRealmAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.TextViewStyler
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.common.util.extensions.forwardTouches
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setPadding
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.compat.SubscriptionManagerCompat
import com.moez.QKSMS.extensions.isSmil
import com.moez.QKSMS.extensions.isText
import com.moez.QKSMS.feature.compose.BubbleUtils.canGroup
import com.moez.QKSMS.feature.compose.BubbleUtils.getBubble
import com.moez.QKSMS.feature.compose.part.PartsAdapter
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.Recipient
import com.moez.QKSMS.util.PhoneNumberUtils
import com.moez.QKSMS.util.Preferences
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.message_list_item_in.*
import kotlinx.android.synthetic.main.message_list_item_in.attachments
import kotlinx.android.synthetic.main.message_list_item_in.body
import kotlinx.android.synthetic.main.message_list_item_in.sim
import kotlinx.android.synthetic.main.message_list_item_in.simIndex
import kotlinx.android.synthetic.main.message_list_item_in.status
import kotlinx.android.synthetic.main.message_list_item_in.timestamp
import kotlinx.android.synthetic.main.message_list_item_in.view.*
import kotlinx.android.synthetic.main.message_list_item_out.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider

class MessagesAdapter @Inject constructor(
    subscriptionManager: SubscriptionManagerCompat,
    private val context: Context,
    private val colors: Colors,
    private val dateFormatter: DateFormatter,
    private val partsAdapterProvider: Provider<PartsAdapter>,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val prefs: Preferences,
    private val textViewStyler: TextViewStyler
) : QkRealmAdapter<Message>() {

    companion object {
        private const val VIEW_TYPE_MESSAGE_IN = 0
        private const val VIEW_TYPE_MESSAGE_OUT = 1

        // Thanks to Cory Kilger for this regex
        // https://gist.github.com/cmkilger/b8f7dba3e76244a84e7e
        private val EMOJI_REGEX = Regex(
                "^[\\s\n\r]*(?:(?:[\u00a9\u00ae\u203c\u2049\u2122\u2139\u2194-\u2199\u21a9-\u21aa\u231a-\u231b\u2328\u23cf\u23e9-\u23f3\u23f8-\u23fa\u24c2\u25aa-\u25ab\u25b6\u25c0\u25fb-\u25fe\u2600-\u2604\u260e\u2611\u2614-\u2615\u2618\u261d\u2620\u2622-\u2623\u2626\u262a\u262e-\u262f\u2638-\u263a\u2648-\u2653\u2660\u2663\u2665-\u2666\u2668\u267b\u267f\u2692-\u2694\u2696-\u2697\u2699\u269b-\u269c\u26a0-\u26a1\u26aa-\u26ab\u26b0-\u26b1\u26bd-\u26be\u26c4-\u26c5\u26c8\u26ce-\u26cf\u26d1\u26d3-\u26d4\u26e9-\u26ea\u26f0-\u26f5\u26f7-\u26fa\u26fd\u2702\u2705\u2708-\u270d\u270f\u2712\u2714\u2716\u271d\u2721\u2728\u2733-\u2734\u2744\u2747\u274c\u274e\u2753-\u2755\u2757\u2763-\u2764\u2795-\u2797\u27a1\u27b0\u27bf\u2934-\u2935\u2b05-\u2b07\u2b1b-\u2b1c\u2b50\u2b55\u3030\u303d\u3297\u3299\ud83c\udc04\ud83c\udccf\ud83c\udd70-\ud83c\udd71\ud83c\udd7e-\ud83c\udd7f\ud83c\udd8e\ud83c\udd91-\ud83c\udd9a\ud83c\ude01-\ud83c\ude02\ud83c\ude1a\ud83c\ude2f\ud83c\ude32-\ud83c\ude3a\ud83c\ude50-\ud83c\ude51\u200d\ud83c\udf00-\ud83d\uddff\ud83d\ude00-\ud83d\ude4f\ud83d\ude80-\ud83d\udeff\ud83e\udd00-\ud83e\uddff\udb40\udc20-\udb40\udc7f]|\u200d[\u2640\u2642]|[\ud83c\udde6-\ud83c\uddff]{2}|.[\u20e0\u20e3\ufe0f]+)+[\\s\n\r]*)+$")

    }

    val clicks: Subject<Long> = PublishSubject.create()
    val partClicks: Subject<Long> = PublishSubject.create()
    val cancelSending: Subject<Long> = PublishSubject.create()

    var data: Pair<Conversation, RealmResults<Message>>? = null
        set(value) {
            if (field === value) return

            field = value
            contactCache.clear()

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
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.body.hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
        }

        val partsAdapter = partsAdapterProvider.get()
        partsAdapter.clicks.subscribe(partClicks)
        view.attachments.adapter = partsAdapter
        view.attachments.setRecycledViewPool(partsViewPool)
        view.body.forwardTouches(view)

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val message = getItem(adapterPosition) ?: return@setOnClickListener
                when (toggleSelection(message.id, false)) {
                    true -> view.isActivated = isSelected(message.id)
                    false -> {
                        clicks.onNext(message.id)
                        expanded[message.id] = view.status.visibility != View.VISIBLE
                        notifyItemChanged(adapterPosition)
                    }
                }
            }
            view.setOnLongClickListener {
                val message = getItem(adapterPosition) ?: return@setOnLongClickListener true
                toggleSelection(message.id)
                view.isActivated = isSelected(message.id)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val message = getItem(position) ?: return
        val previous = if (position == 0) null else getItem(position - 1)
        val next = if (position == itemCount - 1) null else getItem(position + 1)

        val theme = when (message.isOutgoingMessage()) {
            true -> colors.theme()
            false -> colors.theme(contactCache[message.address])
        }

        // Update the selected state
        holder.containerView.isActivated = isSelected(message.id) || highlight == message.id

        // Bind the cancel view
        holder.cancel?.let { cancel ->
            val isCancellable = message.isSending() && message.date > System.currentTimeMillis()
            cancel.setVisible(isCancellable)
            cancel.clicks().subscribe { cancelSending.onNext(message.id) }
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
        bindStatus(holder, message, next)

        // Bind the timestamp
        val timeSincePrevious = TimeUnit.MILLISECONDS.toMinutes(message.date - (previous?.date ?: 0))
        val subscription = subs.find { sub -> sub.subscriptionId == message.subId }

        holder.timestamp.text = dateFormatter.getMessageTimestamp(message.date)
        holder.simIndex.text = subscription?.simSlotIndex?.plus(1)?.toString()

        holder.timestamp.setVisible(timeSincePrevious >= BubbleUtils.TIMESTAMP_THRESHOLD
                || message.subId != previous?.subId && subscription != null)

        holder.sim.setVisible(message.subId != previous?.subId && subscription != null && subs.size > 1)
        holder.simIndex.setVisible(message.subId != previous?.subId && subscription != null && subs.size > 1)

        // Bind the grouping
        val media = message.parts.filter { !it.isSmil() && !it.isText() }
        holder.containerView.setPadding(bottom = if (canGroup(message, next)) 0 else 16.dpToPx(context))

        // Bind the avatar and bubble colour
        if (!message.isMe()) {
            holder.avatar.setRecipient(contactCache[message.address])
            holder.avatar.setVisible(!canGroup(message, next), View.INVISIBLE)

            holder.body.setTextColor(theme.textPrimary)
            holder.body.setBackgroundTint(theme.theme)
        }

        // Bind the body text
        val messageText = when (message.isSms()) {
            true -> message.body
            false -> {
                val subject = message.getCleansedSubject()
                val body = message.parts
                        .filter { part -> part.isText() }
                        .mapNotNull { part -> part.text }
                        .filter { text -> text.isNotBlank() }
                        .joinToString("\n")

                when {
                    subject.isNotBlank() -> {
                        val spannable = SpannableString(if (body.isNotBlank()) "$subject\n$body" else subject)
                        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, subject.length,
                                Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                        spannable
                    }
                    else -> body
                }
            }
        }
        val emojiOnly = messageText.isNotBlank() && messageText.matches(EMOJI_REGEX)
        textViewStyler.setTextSize(holder.body, when (emojiOnly) {
            true -> TextViewStyler.SIZE_EMOJI
            false -> TextViewStyler.SIZE_PRIMARY
        })

        holder.body.text = messageText
        holder.body.setVisible(message.isSms() || messageText.isNotBlank())
        holder.body.setBackgroundResource(getBubble(
                emojiOnly = emojiOnly,
                canGroupWithPrevious = canGroup(message, previous) || media.isNotEmpty(),
                canGroupWithNext = canGroup(message, next),
                isMe = message.isMe()))

        // Bind the attachments
        val partsAdapter = holder.attachments.adapter as PartsAdapter
        partsAdapter.theme = theme
        partsAdapter.setData(message, previous, next, holder)
    }

    private fun bindStatus(holder: QkViewHolder, message: Message, next: Message?) {
        val age = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - message.date)

        holder.status.text = when {
            message.isSending() -> context.getString(R.string.message_status_sending)
            message.isDelivered() -> context.getString(R.string.message_status_delivered,
                    dateFormatter.getTimestamp(message.dateSent))
            message.isFailedMessage() -> context.getString(R.string.message_status_failed)

            // Incoming group message
            !message.isMe() && conversation?.recipients?.size ?: 0 > 1 -> {
                "${contactCache[message.address]?.getDisplayName()} • ${dateFormatter.getTimestamp(message.date)}"
            }

            else -> dateFormatter.getTimestamp(message.date)
        }

        holder.status.setVisible(when {
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
        val message = getItem(position) ?: return -1
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
                set(key, conversation?.recipients?.firstOrNull { phoneNumberUtils.compare(it.address, key) })
            }

            return super.get(key)?.takeIf { it.isValid }
        }

    }
}