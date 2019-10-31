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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.model.Contact
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.contact_chip.view.*
import javax.inject.Inject

class ChipsAdapter @Inject constructor() : QkAdapter<Contact>() {

    var view: RecyclerView? = null
    val chipDeleted: PublishSubject<Contact> = PublishSubject.create<Contact>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.contact_chip, parent, false)
        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val contact = getItem(adapterPosition)
                showDetailedChip(view.context, contact)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val contact = getItem(position)
        val view = holder.containerView

        view.avatar.setContact(contact)

        // If the contact's name is empty, try to display a phone number instead
        // The contacts provided here should only have one number
        view.name.text = if (contact.name.isNotBlank()) {
            contact.name
        } else {
            contact.numbers.firstOrNull { it.address.isNotBlank() }?.address ?: ""
        }
    }

    /**
     * The [context] has to come from a view, because we're inflating a view that used themed attrs
     */
    private fun showDetailedChip(context: Context, contact: Contact) {
        val detailedChipView = DetailedChipView(context)
        detailedChipView.setContact(contact)

        val rootView = view?.rootView as ViewGroup

        val layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        layoutParams.topMargin = 24.dpToPx(context)
        layoutParams.marginStart = 56.dpToPx(context)

        rootView.addView(detailedChipView, layoutParams)
        detailedChipView.show()

        detailedChipView.setOnDeleteListener {
            chipDeleted.onNext(contact)
            detailedChipView.hide()
        }
    }

    override fun areItemsTheSame(old: Contact, new: Contact): Boolean {
        return old.lookupKey == new.lookupKey
    }
}
