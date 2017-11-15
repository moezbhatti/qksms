package com.moez.QKSMS.presentation.main

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ConversationItemTouchCallback @Inject constructor() : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    val swipes: PublishSubject<Int> = PublishSubject.create()

    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipes.onNext(viewHolder.adapterPosition)
    }

}