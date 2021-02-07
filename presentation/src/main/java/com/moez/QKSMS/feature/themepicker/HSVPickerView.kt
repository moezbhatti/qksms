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
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.common.util.extensions.viewBinding
import com.moez.QKSMS.common.util.extensions.within
import com.moez.QKSMS.databinding.HsvPickerViewBinding
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

class HSVPickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    val selectedColor: Subject<Int> = BehaviorSubject.create()

    private val binding = viewBinding(HsvPickerViewBinding::inflate)

    private val hues = arrayOf(0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000)
            .map { it.toInt() }.toIntArray()

    private var min: Float = 0f
    private var max = 0f

    private var hue = 0f
        set(value) {
            field = value
            updateHue()
        }

    init {
        var swatchX = 0f
        var swatchY = 0f

        binding.saturation.setOnTouchListener { _, event ->
            setupBounds()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    swatchX = event.x - event.rawX
                    swatchY = event.y - event.rawY
                    parent.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_MOVE -> {
                    // Calculate the new x/y position
                    binding.swatch.x = (event.rawX + swatchX + min).within(min, max)
                    binding.swatch.y = (event.rawY + swatchY + min).within(min, max)

                    updateSelectedColor()
                }

                MotionEvent.ACTION_UP -> {
                    parent.requestDisallowInterceptTouchEvent(false)
                }

                else -> return@setOnTouchListener false
            }
            true
        }

        var hueThumbX = 0f

        binding.hueGroup.setOnTouchListener { _, event ->
            setupBounds()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    hueThumbX = event.x - event.rawX
                    parent.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_MOVE -> {
                    val x = (event.rawX + hueThumbX + min).within(min, max)

                    binding.hueThumb.x = x
                    hue = (binding.hueThumb.x - min) / (max - min) * 360

                    updateSelectedColor()
                }

                MotionEvent.ACTION_UP -> {
                    parent.requestDisallowInterceptTouchEvent(false)
                }

                else -> return@setOnTouchListener false
            }
            true
        }

        binding.hueTrack.clipToOutline = true
        binding.hueTrack.setImageDrawable(GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, hues))
    }

    private fun setupBounds() {
        if (min == 0f || max == 0f) {
            min = binding.saturation.x - binding.swatch.width / 2
            max = min + binding.saturation.width
        }
    }

    private fun updateSelectedColor() {
        setupBounds()

        val range = max - min
        val hsv = floatArrayOf(hue, (binding.swatch.x - min) / range, 1 - (binding.swatch.y - min) / range)
        val color = Color.HSVToColor(hsv)

        binding.swatch.setTint(color)
        selectedColor.onNext(color)
    }

    fun setColor(color: Int) {
        // Convert the rgb color to HSV
        val hsv = FloatArray(3).apply {
            Color.colorToHSV(color, this)
            hue = this[0]
        }

        // Set the position of the swatch
        post {
            setupBounds()
            val range = max - min

            binding.hueThumb.x = range * hsv[0] / 360 + min
            binding.swatch.x = range * hsv[1] + min
            binding.swatch.y = range * (1 - hsv[2]) + min

            updateSelectedColor()
        }
    }

    private fun updateHue() {
        val hsv = floatArrayOf(hue, 1f, 1f)
        val tint = Color.HSVToColor(hsv)
        binding.saturation.setBackgroundTint(tint)
    }

}