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
package com.moez.QKSMS.common.util.extensions

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.Adapter<*>.autoScrollToStart(recyclerView: RecyclerView) {
    registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

            if (layoutManager.stackFromEnd) {
                if (positionStart > 0) {
                    notifyItemChanged(positionStart - 1)
                }

                val lastPosition = layoutManager.findLastVisibleItemPosition()
                if (positionStart >= getItemCount() - 1 && lastPosition == positionStart - 1) {
                    recyclerView.scrollToPosition(positionStart)
                }
            } else {
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                if (firstVisiblePosition == 0) {
                    recyclerView.scrollToPosition(positionStart)
                }
            }
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

            if (!layoutManager.stackFromEnd) {
                onItemRangeInserted(positionStart, itemCount)
            }
        }
    })
}