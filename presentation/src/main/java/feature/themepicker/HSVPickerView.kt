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

    private var hue = 0f
        set(value) {
            field = value
            updateHue()
        }

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
                    // Calculate the new x/y position
                    val x = (event.rawX + swatchX + min).within(min, max)
                    val y = (event.rawY + swatchY + min).within(min, max)

                    swatch.x = x
                    swatch.y = y

                    val range = max - min
                    val hsv = floatArrayOf(hue, (x - min) / range, 1 - (y - min) / range)
                    swatchPreview.setBackgroundTint(Color.HSVToColor(hsv))
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
        swatchPreview.setBackgroundTint(color)

        // Convert the rgb color to HSV
        val hsv = FloatArray(3).apply {
            Color.colorToHSV(color, this)
            hue = this[0]
        }

        // Set the position of the swatch
        setupBounds()
        val range = max - min
        swatch.x = range * hsv[1] + min
        swatch.y = range * (1 - hsv[2]) + min
    }

    private fun updateHue() {
        val hsv = floatArrayOf(hue, 1f, 1f)
        val tint = Color.HSVToColor(hsv)
        saturation.setBackgroundTint(tint)
    }

}