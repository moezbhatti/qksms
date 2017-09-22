package com.moez.QKSMS.util.extensions

import android.content.res.ColorStateList
import android.widget.ImageView

fun ImageView.setTint(color: Int) {
    imageTintList = ColorStateList.valueOf(color)
}