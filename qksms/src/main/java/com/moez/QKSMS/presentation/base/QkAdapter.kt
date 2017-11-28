package com.moez.QKSMS.presentation.base

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import com.moez.QKSMS.common.util.AbstractDiffUtilCallback

/**
 * Base RecyclerView.Adapter that provides some convenience when creating a new Adapter, such as
 * data list handing and item animations
 */
abstract class QkAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    var data: List<T> = ArrayList()
        set(value) {
            val diff = DiffUtil.calculateDiff(provideDiffUtilCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    fun getItem(position: Int): T {
        return data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }

    /**
     * Allows the adapter implementation to provide a custom DiffUtil.Callback
     * If not, then the abstract implementation will be used
     */
    open fun provideDiffUtilCallback(oldData: List<T>, newData: List<T>): DiffUtil.Callback {
        return AbstractDiffUtilCallback(oldData, newData)
    }

}