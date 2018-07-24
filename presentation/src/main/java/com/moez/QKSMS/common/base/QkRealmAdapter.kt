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
package com.moez.QKSMS.common.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.common.androidxcompat.RealmRecyclerViewAdapter
import com.moez.QKSMS.common.util.extensions.setVisible
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.OrderedRealmCollection
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmResults

abstract class QkRealmAdapter<T : RealmModel> : RealmRecyclerViewAdapter<T, QkViewHolder>(null, true) {

    /**
     * This view can be set, and the adapter will automatically control the visibility of this view
     * based on the data
     */
    var emptyView: View? = null
        set(value) {
            if (field === value) return

            field = value
            value?.setVisible(data?.isLoaded == true && data?.isEmpty() == true)
        }

    private val emptyListener: (OrderedRealmCollection<T>) -> Unit = { data ->
        emptyView?.setVisible(data.isLoaded && data.isEmpty())
    }

    val selectionChanges: Subject<List<Long>> = BehaviorSubject.create()

    private val selection = mutableListOf<Long>()

    /**
     * Toggles the selected state for a particular view
     *
     * If we are currently in selection mode (we have an active selection), then the state will
     * toggle. If we are not in selection mode, then we will only toggle if [force]
     */
    protected fun toggleSelection(id: Long, force: Boolean = true): Boolean {
        if (!force && selection.isEmpty()) return false

        when (selection.contains(id)) {
            true -> selection.remove(id)
            false -> selection.add(id)
        }

        selectionChanges.onNext(selection)
        return true
    }

    protected fun isSelected(id: Long): Boolean {
        return selection.contains(id)
    }

    fun clearSelection() {
        selection.clear()
        selectionChanges.onNext(selection)
        notifyDataSetChanged()
    }

    override fun updateData(data: OrderedRealmCollection<T>?) {
        if (getData() === data) return

        removeListener(getData())
        addListener(data)

        data?.run(emptyListener)

        super.updateData(data)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        addListener(data)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        removeListener(data)
    }

    private fun addListener(data: OrderedRealmCollection<T>?) {
        when (data) {
            is RealmResults<T> -> data.addChangeListener(emptyListener)
            is RealmList<T> -> data.addChangeListener(emptyListener)
        }
    }

    private fun removeListener(data: OrderedRealmCollection<T>?) {
        when (data) {
            is RealmResults<T> -> data.removeChangeListener(emptyListener)
            is RealmList<T> -> data.removeChangeListener(emptyListener)
        }
    }

}