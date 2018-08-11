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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.model.Contact
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.contact_list_item.view.*
import javax.inject.Inject

class ContactAdapter @Inject constructor() : QkAdapter<Contact>() {

    val contactSelected: Subject<Contact> = PublishSubject.create()

    private val numbersViewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.contact_list_item, parent, false)

        view.addresses.setRecycledViewPool(numbersViewPool)

        return QkViewHolder(view).apply {
            view.primary.setOnClickListener {
                val contact = getItem(adapterPosition)
                contactSelected.onNext(copyContact(contact, 0))
            }

            view.addresses.adapter = PhoneNumberAdapter { contact, index ->
                contactSelected.onNext(copyContact(contact, index + 1))
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val contact = getItem(position)
        val view = holder.itemView

        view.avatar.setContact(contact)
        view.name.text = contact.name
        view.name.setVisible(view.name.text.isNotEmpty())
        view.address.text = contact.numbers.first()?.address ?: ""
        view.type.text = contact.numbers.first()?.type ?: ""

        val adapter = view.addresses.adapter as PhoneNumberAdapter
        adapter.contact = contact
        adapter.data = contact.numbers.drop(Math.min(contact.numbers.size, 1))
    }

    /**
     * Creates a copy of the contact with only one phone number, so that the chips
     * view can still display the name/photo, and not get confused about which phone number to use
     */
    private fun copyContact(contact: Contact, numberIndex: Int) = Contact().apply {
        lookupKey = contact.lookupKey
        name = contact.name
        numbers.add(contact.numbers[numberIndex])
    }

    override fun areItemsTheSame(old: Contact, new: Contact): Boolean {
        return old.lookupKey == new.lookupKey
    }

}