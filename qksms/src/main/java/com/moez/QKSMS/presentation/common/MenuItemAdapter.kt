package com.moez.QKSMS.presentation.common

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.data.model.MenuItem
import com.moez.QKSMS.presentation.common.base.QkAdapter
import com.moez.QKSMS.presentation.common.base.QkViewHolder
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.menu_list_item.view.*
import javax.inject.Inject

class MenuItemAdapter @Inject constructor(private val context: Context): QkAdapter<MenuItem>() {

    val menuItemClicks: Subject<Int> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        return QkViewHolder(View.inflate(context, R.layout.menu_list_item, null))
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val menuItem = getItem(position)
        val view = holder.itemView

        view.clicks().subscribe { menuItemClicks.onNext(menuItem.actionId) }

        view.title.setText(menuItem.title)
    }

}