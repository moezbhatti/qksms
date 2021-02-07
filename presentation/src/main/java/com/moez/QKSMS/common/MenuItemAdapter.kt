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
package com.moez.QKSMS.common

import android.content.Context
import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.annotation.ArrayRes
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.databinding.MenuListItemBinding
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

data class MenuItem(val title: String, val actionId: Int)

class MenuItemAdapter @Inject constructor(
    private val context: Context,
    private val colors: Colors
) : QkAdapter<MenuItem, MenuListItemBinding>() {

    val menuItemClicks: Subject<Int> = PublishSubject.create()

    private val disposables = CompositeDisposable()

    var selectedItem: Int? = null
        set(value) {
            val old = data.map { it.actionId }.indexOfFirst { it == field }.takeIf { it != -1 }
            val new = data.map { it.actionId }.indexOfFirst { it == value }.takeIf { it != -1 }

            field = value

            old?.let(::notifyItemChanged)
            new?.let(::notifyItemChanged)
        }

    fun setData(@ArrayRes titles: Int, @ArrayRes values: Int = -1) {
        val valueInts = if (values != -1) context.resources.getIntArray(values) else null

        data = context.resources.getStringArray(titles)
                .mapIndexed { index, title -> MenuItem(title, valueInts?.getOrNull(index) ?: index) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<MenuListItemBinding> {
        return QkViewHolder(parent, MenuListItemBinding::inflate).apply {
            val states = arrayOf(
                    intArrayOf(android.R.attr.state_activated),
                    intArrayOf(-android.R.attr.state_activated))

            val text = parent.context.resolveThemeColor(android.R.attr.textColorTertiary)
            binding.check.imageTintList = ColorStateList(states, intArrayOf(colors.theme().theme, text))

            binding.root.setOnClickListener {
                val menuItem = getItem(adapterPosition)
                menuItemClicks.onNext(menuItem.actionId)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder<MenuListItemBinding>, position: Int) {
        val menuItem = getItem(position)

        holder.binding.title.text = menuItem.title
        holder.binding.check.isActivated = (menuItem.actionId == selectedItem)
        holder.binding.check.setVisible(selectedItem != null)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

}