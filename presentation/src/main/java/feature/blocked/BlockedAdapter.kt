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
package feature.blocked

import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import common.base.QkRealmAdapter
import common.base.QkViewHolder
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.blocked_list_item.view.*
import model.Conversation
import javax.inject.Inject

class BlockedAdapter @Inject constructor() : QkRealmAdapter<Conversation>() {

    val unblock: PublishSubject<Long> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        return QkViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.blocked_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val conversation = getItem(position)!!
        val view = holder.itemView

        view.setOnClickListener { unblock.onNext(conversation.id) }

        view.avatars.contacts = conversation.recipients
        view.title.text = conversation.getTitle()
    }

}