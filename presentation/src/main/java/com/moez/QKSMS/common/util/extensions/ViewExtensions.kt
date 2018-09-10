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
package com.moez.QKSMS.common.util.extensions

import android.animation.LayoutTransition
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager

var ViewGroup.animateLayoutChanges: Boolean
    get() = layoutTransition != null
    set(value) {
        layoutTransition = if (value) LayoutTransition() else null
    }


fun EditText.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

fun ImageView.setTint(color: Int) {
    imageTintList = ColorStateList.valueOf(color)
}

fun ProgressBar.setTint(color: Int) {
    indeterminateTintList = ColorStateList.valueOf(color)
    progressTintList = ColorStateList.valueOf(color)
}

fun View.setBackgroundTint(color: Int) {

    // API 21 doesn't support this
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
        background?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    backgroundTintList = ColorStateList.valueOf(color)
}

fun View.setPadding(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    setPadding(left ?: paddingLeft, top ?: paddingTop, right ?: paddingRight, bottom ?: paddingBottom)
}

fun View.setVisible(visible: Boolean, invisible: Int = View.GONE) {
    visibility = if (visible) View.VISIBLE else invisible
}

/**
 * If a view captures clicks at all, then the parent won't ever receive touch events. This is a
 * problem when we're trying to capture link clicks, but tapping or long pressing other areas of
 * the view no longer work. Also problematic when we try to long press on an image in the message
 * view
 */
fun View.forwardTouches(parent: View) {
    var isLongClick = false

    setOnLongClickListener {
        isLongClick = true
        true
    }

    setOnTouchListener { v, event ->
        parent.onTouchEvent(event)

        when {
            event.action == MotionEvent.ACTION_UP && isLongClick -> {
                isLongClick = true
                true
            }

            event.action == MotionEvent.ACTION_DOWN -> {
                isLongClick = false
                v.onTouchEvent(event)
            }

            else -> v.onTouchEvent(event)
        }
    }
}

fun ViewPager.addOnPageChangeListener(listener: (Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            listener(position)
        }
    })
}

fun RecyclerView.scrapViews() {
    val adapter = adapter
    val layoutManager = layoutManager

    this.adapter = null
    this.layoutManager = null

    this.adapter = adapter
    this.layoutManager = layoutManager

    adapter?.notifyDataSetChanged()
}
