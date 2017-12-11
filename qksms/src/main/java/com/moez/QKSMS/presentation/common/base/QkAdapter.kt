package com.moez.QKSMS.presentation.common.base

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

    open protected fun areItemsTheSame(old: T, new: T): Boolean {
        return old == new
    }

    open protected fun areContentsTheSame(old: T, new: T): Boolean {
        return old == new
    }

}