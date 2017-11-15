package com.moez.QKSMS.common.util

import android.support.v7.util.DiffUtil

/**
 * Abstract implementation of DiffUtil.Callback, which can be used when we don't need
 * a detailed change payload
 *
 * If we need a detailed change payload, this class can be extended and #areContentsTheSame
 * and #getChangePayload should overridden
 */
class AbstractDiffUtilCallback<T>(
        private val oldList: List<T>,
        private val newList: List<T>) :
        DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}