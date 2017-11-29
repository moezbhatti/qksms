package com.moez.QKSMS.common.util.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
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
    backgroundTintList = ColorStateList.valueOf(color)
}

fun View.setMargins(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    val lp = layoutParams as? ViewGroup.MarginLayoutParams ?: return

    lp.setMargins(
            left ?: lp.leftMargin,
            top ?: lp.topMargin,
            right ?: lp.rightMargin,
            bottom ?: lp.rightMargin)

    layoutParams = lp
}

fun View.setPadding(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    setPadding(left ?: paddingLeft, top ?: paddingTop, right ?: paddingRight, bottom ?: paddingBottom)
}

fun View.setVisible(visible: Boolean, invisible: Int = View.GONE) {
    visibility = if (visible) View.VISIBLE else invisible
}