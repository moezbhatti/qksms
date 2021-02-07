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

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.forwardTouches
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.databinding.ContactListItemBinding
import com.moez.QKSMS.extensions.associateByNotNull
import com.moez.QKSMS.model.Contact
import com.moez.QKSMS.model.ContactGroup
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Recipient
import com.moez.QKSMS.repository.ConversationRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ComposeItemAdapter @Inject constructor(
    private val colors: Colors,
    private val conversationRepo: ConversationRepository
) : QkAdapter<ComposeItem, ContactListItemBinding>() {

    val clicks: Subject<ComposeItem> = PublishSubject.create()
    val longClicks: Subject<ComposeItem> = PublishSubject.create()

    private val numbersViewPool = RecyclerView.RecycledViewPool()
    private val disposables = CompositeDisposable()

    var recipients: Map<String, Recipient> = mapOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<ContactListItemBinding> {
        return QkViewHolder(parent, ContactListItemBinding::inflate).apply {
            binding.icon.setTint(colors.theme().theme)

            binding.numbers.setRecycledViewPool(numbersViewPool)
            binding.numbers.adapter = PhoneNumberAdapter()
            binding.numbers.forwardTouches(binding.root)

            binding.root.setOnClickListener {
                val item = getItem(adapterPosition)
                clicks.onNext(item)
            }
            binding.root.setOnLongClickListener {
                val item = getItem(adapterPosition)
                longClicks.onNext(item)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder<ContactListItemBinding>, position: Int) {
        val prevItem = if (position > 0) getItem(position - 1) else null
        val item = getItem(position)

        when (item) {
            is ComposeItem.New -> bindNew(holder, item.value)
            is ComposeItem.Recent -> bindRecent(holder, item.value, prevItem)
            is ComposeItem.Starred -> bindStarred(holder, item.value, prevItem)
            is ComposeItem.Person -> bindPerson(holder, item.value, prevItem)
            is ComposeItem.Group -> bindGroup(holder, item.value, prevItem)
        }
    }

    private fun bindNew(holder: QkViewHolder<ContactListItemBinding>, contact: Contact) {
        holder.binding.index.isVisible = false

        holder.binding.icon.isVisible = false

        holder.binding.avatar.recipients = listOf(createRecipient(contact))

        holder.binding.title.text = contact.numbers.joinToString { it.address }

        holder.binding.subtitle.isVisible = false

        holder.binding.numbers.isVisible = false
    }

    private fun bindRecent(holder: QkViewHolder<ContactListItemBinding>, conversation: Conversation, prev: ComposeItem?) {
        holder.binding.index.isVisible = false

        holder.binding.icon.isVisible = prev !is ComposeItem.Recent
        holder.binding.icon.setImageResource(R.drawable.ic_history_black_24dp)

        holder.binding.avatar.recipients = conversation.recipients

        holder.binding.title.text = conversation.getTitle()

        holder.binding.subtitle.isVisible = conversation.recipients.size > 1 && conversation.name.isBlank()
        holder.binding.subtitle.text = conversation.recipients.joinToString(", ") { recipient ->
            recipient.contact?.name ?: recipient.address
        }
        holder.binding.subtitle.collapseEnabled = conversation.recipients.size > 1

        holder.binding.numbers.isVisible = conversation.recipients.size == 1
        (holder.binding.numbers.adapter as PhoneNumberAdapter).data = conversation.recipients
                .mapNotNull { recipient -> recipient.contact }
                .flatMap { contact -> contact.numbers }
    }

    private fun bindStarred(holder: QkViewHolder<ContactListItemBinding>, contact: Contact, prev: ComposeItem?) {
        holder.binding.index.isVisible = false

        holder.binding.icon.isVisible = prev !is ComposeItem.Starred
        holder.binding.icon.setImageResource(R.drawable.ic_star_black_24dp)

        holder.binding.avatar.recipients = listOf(createRecipient(contact))

        holder.binding.title.text = contact.name

        holder.binding.subtitle.isVisible = false

        holder.binding.numbers.isVisible = true
        (holder.binding.numbers.adapter as PhoneNumberAdapter).data = contact.numbers
    }

    private fun bindGroup(holder: QkViewHolder<ContactListItemBinding>, group: ContactGroup, prev: ComposeItem?) {
        holder.binding.index.isVisible = false

        holder.binding.icon.isVisible = prev !is ComposeItem.Group
        holder.binding.icon.setImageResource(R.drawable.ic_people_black_24dp)

        holder.binding.avatar.recipients = group.contacts.map(::createRecipient)

        holder.binding.title.text = group.title

        holder.binding.subtitle.isVisible = true
        holder.binding.subtitle.text = group.contacts.joinToString(", ") { it.name }
        holder.binding.subtitle.collapseEnabled = group.contacts.size > 1

        holder.binding.numbers.isVisible = false
    }

    private fun bindPerson(holder: QkViewHolder<ContactListItemBinding>, contact: Contact, prev: ComposeItem?) {
        holder.binding.index.isVisible = true
        holder.binding.index.text = if (contact.name.getOrNull(0)?.isLetter() == true) contact.name[0].toString() else "#"
        holder.binding.index.isVisible = prev !is ComposeItem.Person ||
                (contact.name[0].isLetter() && !contact.name[0].equals(prev.value.name[0], ignoreCase = true)) ||
                (!contact.name[0].isLetter() && prev.value.name[0].isLetter())

        holder.binding.icon.isVisible = false

        holder.binding.avatar.recipients = listOf(createRecipient(contact))

        holder.binding.title.text = contact.name

        holder.binding.subtitle.isVisible = false

        holder.binding.numbers.isVisible = true
        (holder.binding.numbers.adapter as PhoneNumberAdapter).data = contact.numbers
    }

    private fun createRecipient(contact: Contact): Recipient {
        return recipients[contact.lookupKey] ?: Recipient(
            address = contact.numbers.firstOrNull()?.address ?: "",
            contact = contact)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        disposables += conversationRepo.getUnmanagedRecipients()
                .map { recipients -> recipients.associateByNotNull { recipient -> recipient.contact?.lookupKey } }
                .subscribe { recipients -> this@ComposeItemAdapter.recipients = recipients }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        disposables.clear()
    }

    override fun areItemsTheSame(old: ComposeItem, new: ComposeItem): Boolean {
        val oldIds = old.getContacts().map { contact -> contact.lookupKey }
        val newIds = new.getContacts().map { contact -> contact.lookupKey }
        return oldIds == newIds
    }

    override fun areContentsTheSame(old: ComposeItem, new: ComposeItem): Boolean {
        return false
    }

}
