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
package com.moez.QKSMS.feature.changelog

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import kotlinx.android.synthetic.main.changelog_list_item.*

class ChangelogAdapter(val keywords: List<String>, data: List<String>) : QkAdapter<String>() {

    init {
        this.data = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.changelog_list_item, parent, false)
        return QkViewHolder(view).apply {
            if (viewType == 0) {
                changelogItem.setTypeface(changelogItem.typeface, Typeface.BOLD)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val item = getItem(position)

        holder.changelogItem.text = item
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) in keywords) 0 else 1
    }

}
