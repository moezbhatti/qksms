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
package com.moez.QKSMS.feature.conversationinfo

import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkRealmAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.model.Recipient
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.conversation_recipient_list_item.view.*
import javax.inject.Inject

class ConversationRecipientAdapter @Inject constructor(
    private val colors: Colors
) : QkRealmAdapter<Recipient>() {

    val recipientClicks: Subject<Long> = PublishSubject.create()
    val recipientLongClicks: Subject<Long> = PublishSubject.create()
    val themeClicks: Subject<Long> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.conversation_recipient_list_item, parent, false)
        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val recipient = getItem(adapterPosition) ?: return@setOnClickListener
                recipientClicks.onNext(recipient.id)
            }

            view.setOnLongClickListener {
                val recipient = getItem(adapterPosition) ?: return@setOnLongClickListener false
                recipientLongClicks.onNext(recipient.id)
                return@setOnLongClickListener true
            }

            view.theme.setOnClickListener {
                val recipient = getItem(adapterPosition) ?: return@setOnClickListener
                themeClicks.onNext(recipient.id)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val recipient = getItem(position) ?: return
        val view = holder.containerView

        view.avatar.setRecipient(recipient)

        view.name.text = recipient.contact?.name ?: recipient.address

        view.address.text = recipient.address
        view.address.setVisible(recipient.contact != null)

        view.add.setVisible(recipient.contact == null)

        val theme = colors.theme(recipient)
        view.theme.setTint(theme.theme)
    }

}
