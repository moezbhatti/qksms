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
package common.util.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView

fun EditText.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

fun ImageView.setTint(color: Int) {
    imageTintList = ColorStateList.valueOf(color)
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

