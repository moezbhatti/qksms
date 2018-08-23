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
package com.moez.QKSMS.feature.backup

import android.content.Context
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.FlowableAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.model.Backup
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.backup_list_item.view.*
import javax.inject.Inject

class BackupAdapter @Inject constructor(
        private val context: Context,
        private val dateFormatter: DateFormatter
) : FlowableAdapter<Backup>() {

    val backupSelected: Subject<Backup> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.backup_list_item, parent, false)

        return QkViewHolder(view).apply {
            view.setOnClickListener { backupSelected.onNext(getItem(adapterPosition)) }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val backup = getItem(position)
        val view = holder.itemView

        val count = backup.messages.size

        view.title.text = dateFormatter.getDetailedTimestamp(backup.date)
        view.messages.text = context.resources.getQuantityString(R.plurals.backup_message_count, count, count)
        view.size.text = Formatter.formatFileSize(context, backup.size)
    }

}