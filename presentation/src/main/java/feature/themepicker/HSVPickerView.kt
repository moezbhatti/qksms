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
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.moez.QKSMS.R
import common.util.extensions.setBackgroundTint
import common.util.extensions.within
import kotlinx.android.synthetic.main.hsv_picker_view.view.*

class HSVPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    private var min: Float = 0f
    private var max = 0f

    init {
        View.inflate(context, R.layout.hsv_picker_view, this)

        var swatchX = 0f
        var swatchY = 0f

        saturation.setOnTouchListener { _, event ->
            setupBounds()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    swatchX = event.x - event.rawX
                    swatchY = event.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    swatch.x = (event.rawX + swatchX + min).within(min, max)
                    swatch.y = (event.rawY + swatchY + min).within(min, max)
                }

                else -> return@setOnTouchListener false
            }
            true
        }
    }

    private fun setupBounds() {
        if (min == 0f || max == 0f) {
            min = saturation.x - swatch.width / 2
            max = min + saturation.width
        }
    }

    fun setColor(color: Int) {

        // Convert the rgb color to HSV
        val hsv = FloatArray(3).apply { Color.colorToHSV(color, this) }

        // Set the position of the swatch
        setupBounds()
        val range = max - min
        swatch.x = range * hsv[1] + min
        swatch.y = range * (1 - hsv[2]) + min

        // Change the HSV saturation and lightness, for tinting the gradient
        hsv[1] = 1f
        hsv[2] = 1f

        swatchPreview.setBackgroundTint(color)
        saturation.setBackgroundTint(Color.HSVToColor(hsv))
    }

}