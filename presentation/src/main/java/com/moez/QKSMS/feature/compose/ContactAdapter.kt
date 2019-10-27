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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.extensions.forwardTouches
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
        view.addresses.adapter = PhoneNumberAdapter()
        view.addresses.forwardTouches(view)

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val contact = getItem(adapterPosition)
                contactSelected.onNext(contact)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val prevContact = if (position > 0) getItem(position - 1) else null
        val contact = getItem(position)
        val view = holder.containerView

        view.index.text = if (contact.name.getOrNull(0)?.isLetter() == true) contact.name[0].toString() else "#"
        view.index.isVisible = prevContact == null ||
                (contact.name[0].isLetter() && contact.name[0] != prevContact.name[0]) ||
                (!contact.name[0].isLetter() && prevContact.name[0].isLetter())

        view.avatar.setContact(contact)
        view.name.text = contact.name
        view.name.setVisible(view.name.text.isNotEmpty())

        (view.addresses.adapter as PhoneNumberAdapter).data = contact.numbers
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

    override fun areContentsTheSame(old: Contact, new: Contact): Boolean = false

}
