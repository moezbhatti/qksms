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
package com.moez.QKSMS.feature.themepicker

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.databinding.ThemeListItemBinding
import com.moez.QKSMS.databinding.ThemePaletteListItemBinding
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ThemeAdapter @Inject constructor(
    private val context: Context,
    private val colors: Colors
) : QkAdapter<List<Int>, ThemePaletteListItemBinding>() {

    val colorSelected: Subject<Int> = PublishSubject.create()

    var selectedColor: Int = -1
        set(value) {
            val oldPosition = data.indexOfFirst { it.contains(field) }
            val newPosition = data.indexOfFirst { it.contains(value) }

            field = value
            iconTint = colors.textPrimaryOnThemeForColor(value)

            oldPosition.takeIf { it != -1 }?.let { position -> notifyItemChanged(position) }
            newPosition.takeIf { it != -1 }?.let { position -> notifyItemChanged(position) }
        }

    private var iconTint = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<ThemePaletteListItemBinding> {
        return QkViewHolder(parent, ThemePaletteListItemBinding::inflate).apply {
            binding.palette.flexWrap = FlexWrap.WRAP
            binding.palette.flexDirection = FlexDirection.ROW
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder<ThemePaletteListItemBinding>, position: Int) {
        val palette = getItem(position)

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val minPadding = (16 * 6).dpToPx(context)
        val size = if (screenWidth - minPadding > (56 * 5).dpToPx(context)) {
            56.dpToPx(context)
        } else {
            (screenWidth - minPadding) / 5
        }
        val swatchPadding = (screenWidth - size * 5) / 12

        holder.binding.palette.removeAllViews()
        holder.binding.palette.setPadding(swatchPadding, swatchPadding, swatchPadding, swatchPadding)

        (palette.subList(0, 5) + palette.subList(5, 10).reversed())
                .mapIndexed { index, color ->
                    ThemeListItemBinding.inflate(LayoutInflater.from(context), holder.binding.palette, false).apply {

                        // Send clicks to the selected subject
                        root.setOnClickListener { colorSelected.onNext(color) }

                        // Apply the color to the view
                        theme.setBackgroundTint(color)

                        // Control the check visibility and tint
                        check.setVisible(color == selectedColor)
                        check.setTint(iconTint)

                        // Update the size so that the spacing is perfectly even
                        root.layoutParams = (root.layoutParams as FlexboxLayout.LayoutParams).apply {
                            height = size
                            width = size
                            isWrapBefore = index % 5 == 0
                            setMargins(swatchPadding, swatchPadding, swatchPadding, swatchPadding)
                        }
                    }.root
                }
                .forEach { theme -> holder.binding.palette.addView(theme) }
    }

}