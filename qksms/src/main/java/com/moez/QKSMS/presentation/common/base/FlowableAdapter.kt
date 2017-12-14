package com.moez.QKSMS.presentation.common.base

import android.support.annotation.CallSuper
import android.support.v7.widget.RecyclerView
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable

/**
 * Base RecyclerView.Adapter that provides some convenience when creating a new Adapter, such as
 * data list handing and item animations
 */
abstract class FlowableAdapter<T> : QkAdapter<T>() {

    var flowable: Flowable<List<T>>? = null
        set(value) {
            field = value

            // Stop listening for updates on the old flowable
            dispose()

            // Wipe the data
            data = ArrayList()

            // If we're attached to any RecyclerViews, then subscribe to updates
            if (recyclerViews.isNotEmpty()) {
                subscribe()
            }
        }

    private val recyclerViews = ArrayList<RecyclerView>()
    private var disposable: Disposable? = null

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {

        // If this is the first RecyclerView to be attached, then start listening to updates
        if (recyclerViews.isEmpty() || disposable == null) {
            subscribe()
        }

        recyclerViews.add(recyclerView)
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        recyclerViews.remove(recyclerView)

        // If no more RecyclerViews are attached, stop listening to updates
        if (recyclerViews.isEmpty()) {
            dispose()
        }
    }

    private fun subscribe() {
        disposable = flowable?.subscribe { data = it }
    }

    private fun dispose() {
        disposable?.dispose()
    }

}