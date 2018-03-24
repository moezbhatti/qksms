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
package feature.themepicker

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.graphics.ColorUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.moez.QKSMS.R
import common.util.extensions.setBackgroundTint
import common.util.extensions.within
import kotlinx.android.synthetic.main.hsl_picker_view.view.*

class HSLPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    private var swatchX = 0f
    private var swatchY = 0f

    init {
        View.inflate(context, R.layout.hsl_picker_view, this)

        saturation.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    swatchX = event.x - event.rawX
                    swatchY = event.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    val min = saturation.x - swatch.width / 2
                    val max = min + saturation.width

                    swatch.x = (event.rawX + swatchX + min).within(min, max)
                    swatch.y = (event.rawY + swatchY + min).within(min, max)
                }

                else -> return@setOnTouchListener false
            }
            true
        }
    }

    fun setColor(color: Int) {
        swatchPreview.setBackgroundTint(color)

        val hsl = FloatArray(3).apply {
            ColorUtils.colorToHSL(color, this)
            this[1] = 1f
            this[2] = 0.5f
        }

        saturation.setBackgroundTint(ColorUtils.HSLToColor(hsl))
    }

}