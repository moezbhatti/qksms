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
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.model.Contact
import com.moez.QKSMS.model.PhoneNumber
import kotlinx.android.synthetic.main.contact_list_item.view.*

class PhoneNumberAdapter(
        private val numberClicked: (Contact, Int) -> Unit
) : QkAdapter<PhoneNumber>() {

    lateinit var contact: Contact

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.contact_number_list_item, parent, false)
        return QkViewHolder(view)
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val number = getItem(position)
        val view = holder.itemView

        // Setting this in onCreateViewHolder causes a crash sometimes. [contact] returns the
        // contact from a different row, I'm not sure why
        view.setOnClickListener { numberClicked(contact, position) }

        view.address.text = number.address
        view.type.text = number.type
    }

}