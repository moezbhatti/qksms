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
package common.base

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView

/**
 * Base RecyclerView.Adapter that provides some convenience when creating a new Adapter, such as
 * data list handing and item animations
 */
abstract class QkAdapter<T> : RecyclerView.Adapter<QkViewHolder>() {

    var data: List<T> = ArrayList()
        set(value) {
            val diff = DiffUtil.calculateDiff(getDiffUtilCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
            onDatasetChanged()
        }

    fun getItem(position: Int): T {
        return data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }

    open fun onDatasetChanged() {}

    /**
     * Allows the adapter implementation to provide a custom DiffUtil.Callback
     * If not, then the abstract implementation will be used
     */
    private fun getDiffUtilCallback(oldData: List<T>, newData: List<T>): DiffUtil.Callback {
        return object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    areItemsTheSame(oldData[oldItemPosition], newData[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    areContentsTheSame(oldData[oldItemPosition], newData[newItemPosition])

            override fun getOldListSize() = oldData.size

            override fun getNewListSize() = newData.size
        }
    }

    protected open fun areItemsTheSame(old: T, new: T): Boolean {
        return old == new
    }

    protected open fun areContentsTheSame(old: T, new: T): Boolean {
        return old == new
    }

}