/*
 * Copyright (C) 2019 Moez Bhatti <moez.bhatti@gmail.com>
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
package com.moez.QKSMS.feature.compose.editing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.forwardTouches
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.model.Contact
import com.moez.QKSMS.model.ContactGroup
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Recipient
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.contact_list_item.view.*
import javax.inject.Inject

class ComposeItemAdapter @Inject constructor(private val colors: Colors) : QkAdapter<ComposeItem>() {

    val clicks: Subject<ComposeItem> = PublishSubject.create()
    val longClicks: Subject<ComposeItem> = PublishSubject.create()

    private val numbersViewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.contact_list_item, parent, false)

        view.icon.setTint(colors.theme().theme)

        view.numbers.setRecycledViewPool(numbersViewPool)
        view.numbers.adapter = PhoneNumberAdapter()
        view.numbers.forwardTouches(view)

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val item = getItem(adapterPosition)
                clicks.onNext(item)
            }
            view.setOnLongClickListener {
                val item = getItem(adapterPosition)
                longClicks.onNext(item)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val prevItem = if (position > 0) getItem(position - 1) else null
        val item = getItem(position)
        val view = holder.containerView

        when (item) {
            is ComposeItem.New -> bindNew(view, item.value)
            is ComposeItem.Recent -> bindRecent(view, item.value, prevItem)
            is ComposeItem.Starred -> bindStarred(view, item.value, prevItem)
            is ComposeItem.Person -> bindPerson(view, item.value, prevItem)
            is ComposeItem.Group -> bindGroup(view, item.value, prevItem)
        }
    }

    private fun bindNew(view: View, contact: Contact) {
        view.index.isVisible = false

        view.icon.isVisible = false

        view.avatar.contacts = listOf(Recipient(contact = contact))

        view.title.text = contact.numbers.joinToString { it.address }

        view.subtitle.isVisible = false

        view.numbers.isVisible = false
    }

    private fun bindRecent(view: View, conversation: Conversation, prev: ComposeItem?) {
        view.index.isVisible = false

        view.icon.isVisible = prev !is ComposeItem.Recent
        view.icon.setImageResource(R.drawable.ic_history_black_24dp)

        view.avatar.contacts = conversation.recipients

        view.title.text = conversation.getTitle()

        view.subtitle.isVisible = conversation.recipients.size > 1 && conversation.name.isBlank()
        view.subtitle.text = conversation.recipients.joinToString(", ") { recipient ->
            recipient.contact?.name ?: recipient.address
        }

        view.numbers.isVisible = conversation.recipients.size == 1
        (view.numbers.adapter as PhoneNumberAdapter).data = conversation.recipients
                .mapNotNull { recipient -> recipient.contact }
                .flatMap { contact -> contact.numbers }
    }

    private fun bindStarred(view: View, contact: Contact, prev: ComposeItem?) {
        view.index.isVisible = false

        view.icon.isVisible = prev !is ComposeItem.Starred
        view.icon.setImageResource(R.drawable.ic_star_black_24dp)

        view.avatar.contacts = listOf(Recipient(contact = contact))

        view.title.text = contact.name

        view.subtitle.isVisible = false

        view.numbers.isVisible = true
        (view.numbers.adapter as PhoneNumberAdapter).data = contact.numbers
    }

    private fun bindGroup(view: View, group: ContactGroup, prev: ComposeItem?) {
        view.index.isVisible = false

        view.icon.isVisible = prev !is ComposeItem.Group
        view.icon.setImageResource(R.drawable.ic_people_black_24dp)

        view.avatar.contacts = group.contacts.map { contact -> Recipient(contact = contact) }

        view.title.text = group.title

        view.subtitle.isVisible = true
        view.subtitle.text = group.contacts.joinToString(", ") { it.name }

        view.numbers.isVisible = false
    }

    private fun bindPerson(view: View, contact: Contact, prev: ComposeItem?) {
        view.index.isVisible = true
        view.index.text = if (contact.name.getOrNull(0)?.isLetter() == true) contact.name[0].toString() else "#"
        view.index.isVisible = prev !is ComposeItem.Person ||
                (contact.name[0].isLetter() && !contact.name[0].equals(prev.value.name[0], ignoreCase = true)) ||
                (!contact.name[0].isLetter() && prev.value.name[0].isLetter())

        view.icon.isVisible = false

        view.avatar.contacts = listOf(Recipient(contact = contact))

        view.title.text = contact.name

        view.subtitle.isVisible = false

        view.numbers.isVisible = true
        (view.numbers.adapter as PhoneNumberAdapter).data = contact.numbers
    }

    override fun areContentsTheSame(old: ComposeItem, new: ComposeItem): Boolean = false

}
