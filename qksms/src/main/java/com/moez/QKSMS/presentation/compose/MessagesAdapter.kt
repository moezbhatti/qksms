package com.moez.QKSMS.presentation.compose

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setPadding
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.presentation.base.QkViewHolder
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.message_list_item_in.view.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessagesAdapter : RealmRecyclerViewAdapter<Message, QkViewHolder>(null, true) {

    companion object {
        private val VIEWTYPE_ME = -1
        private val TIMESTAMP_THRESHOLD = 60
    }

    @Inject lateinit var context: Context
    @Inject lateinit var themeManager: ThemeManager
    @Inject lateinit var dateFormatter: DateFormatter

    val longClicks: Subject<Message> = PublishSubject.create<Message>()

    private val people = ArrayList<String>()

    init {
        AppComponentManager.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): QkViewHolder {
        val layoutRes: Int
        val bubbleColor: Int
        when (viewType) {
            VIEWTYPE_ME -> {
                layoutRes = R.layout.message_list_item_out
                bubbleColor = themeManager.bubbleColor
            }
            else -> {
                layoutRes = R.layout.message_list_item_in
                bubbleColor = themeManager.color
            }
        }

        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(layoutRes, parent, false)

        view.body.setBackgroundTint(bubbleColor)

        if (viewType != VIEWTYPE_ME) {
            val contact = Contact()
            contact.address = people[viewType]
            view.avatar.contact = contact
        }

        return QkViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val message = getItem(position)!!
        val view = viewHolder.itemView

        RxView.clicks(view).subscribe { Timber.v(message.toString()) }
        RxView.longClicks(view).subscribe { longClicks.onNext(message) }

        bindGrouping(view, position)

        view.status?.visibility = when {
            message.isSending() || message.isFailedMessage() -> View.VISIBLE
            else -> View.GONE
        }

        view.status?.text = when {
            message.isSending() -> "Sending..."
            message.isFailedMessage() -> "Failed to send. Tap to try again"
            else -> null
        }

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
                view.body.setBackgroundResource(if (sent) R.drawable.message_out_first else R.drawable.message_in_first)
                view.avatar?.visibility = View.INVISIBLE
            }
            canGroupWithPrevious && canGroupWithNext -> {
                view.setPadding(bottom = 2.dpToPx(context))
                view.body.setBackgroundResource(if (sent) R.drawable.message_out_middle else R.drawable.message_in_middle)
                view.avatar?.visibility = View.INVISIBLE
            }
            canGroupWithPrevious && !canGroupWithNext -> {
                view.setPadding(bottom = 16.dpToPx(context))
                view.body.setBackgroundResource(if (sent) R.drawable.message_out_last else R.drawable.message_in_last)
                view.avatar?.visibility = View.VISIBLE
            }
            else -> {
                view.setPadding(bottom = 16.dpToPx(context))
                view.body.setBackgroundResource(R.drawable.message_only)
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