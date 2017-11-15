package com.moez.QKSMS.presentation.base

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import com.moez.QKSMS.common.util.AbstractDiffUtilCallback
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable

/**
 * Base RecyclerView.Adapter that provides some convenience when creating a new Adapter, such as
 * data list handing and item animations
 *
 * If Sections are required for the RecyclerView UI, please use the BaseSectionAdapter
 */
abstract class QkAdapter<T, VH : RecyclerView.ViewHolder>(private val flowable: Flowable<List<T>>) : RecyclerView.Adapter<VH>() {

    private var disposable: Disposable? = null

    private var data: List<T> = ArrayList()
        set(value) {
            DiffUtil.calculateDiff(provideDiffUtilCallback(field, value)).dispatchUpdatesTo(this)
            field = value
        }

    fun getItem(position: Int): T {
        return data[position]
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        disposable = flowable.subscribe { data = it }

    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposable?.dispose()
    }

    override final fun getItemCount(): Int {
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