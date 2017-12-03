package com.moez.QKSMS.presentation.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.presentation.base.QkAdapter
import com.moez.QKSMS.presentation.base.QkViewHolder
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.theme_list_item.view.*

class ThemeAdapter(private val context: Context) : QkAdapter<Int>() {

    val colorSelected: Subject<Int> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.theme_list_item, parent, false)
        return QkViewHolder(view)
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val theme = getItem(position)
        val view = holder.itemView

        view.setOnClickListener { colorSelected.onNext(theme) }

        view.theme.setBackgroundTint(theme)
    }

}