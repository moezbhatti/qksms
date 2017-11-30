package com.moez.QKSMS.presentation.base

import android.support.v7.widget.RecyclerView
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable

/**
 * Base RecyclerView.Adapter that provides some convenience when creating a new Adapter, such as
 * data list handing and item animations
 */
abstract class FlowableAdapter<T>(private val flowable: Flowable<List<T>>) : QkAdapter<T>() {

    private var disposable: Disposable? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        disposable = flowable.subscribe { data = it }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposable?.dispose()
    }

}