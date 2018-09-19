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
package com.moez.QKSMS.feature.scheduled

import android.net.Uri
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkRealmAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.model.Contact
import com.moez.QKSMS.model.Recipient
import com.moez.QKSMS.model.ScheduledMessage
import com.moez.QKSMS.repository.ContactRepository
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.scheduled_message_list_item.view.*
import javax.inject.Inject

class ScheduledMessageAdapter @Inject constructor(
        private val contactRepo: ContactRepository,
        private val dateFormatter: DateFormatter
) : QkRealmAdapter<ScheduledMessage>() {

    private val contacts by lazy { contactRepo.getContacts() }
    private val contactCache = ContactCache()
    private val imagesViewPool = RecyclerView.RecycledViewPool()

    val clicks: Subject<Long> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scheduled_message_list_item, parent, false)

        view.attachments.adapter = ScheduledMessageAttachmentAdapter()
        view.attachments.setRecycledViewPool(imagesViewPool)

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val message = getItem(adapterPosition)!!
                clicks.onNext(message.id)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val message = getItem(position)!!
        val view = holder.itemView

        // GroupAvatarView only accepts recipients, so map the phone numbers to recipients
        view.avatars.contacts = message.recipients.map { address -> Recipient(address = address) }

        view.recipients.text = message.recipients.joinToString(",") { address ->
            contactCache[address]?.name?.takeIf { it.isNotBlank() } ?: address
        }

        view.date.text = dateFormatter.getScheduledTimestamp(message.date)
        view.body.text = message.body

        val adapter = view.attachments.adapter as ScheduledMessageAttachmentAdapter
        adapter.data = message.attachments.map(Uri::parse)
        view.attachments.isVisible = message.attachments.isNotEmpty()
    }

    /**
     * Cache the contacts in a map by the address, because the messages we're binding don't have
     * a reference to the contact.
     */
    private inner class ContactCache : HashMap<String, Contact?>() {

        override fun get(key: String): Contact? {
            if (super.get(key)?.isValid != true) {
                set(key, contacts.firstOrNull { contact ->
                    contact.numbers.any {
                        PhoneNumberUtils.compare(it.address, key)
                    }
                })
            }

            return super.get(key)?.takeIf { it.isValid }
        }

    }

}