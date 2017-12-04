package com.moez.QKSMS.common.util.extensions

import android.content.Context
import android.graphics.Color
import android.util.TypedValue

fun Int.dpToPx(context: Context): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).toInt()
}

fun Int.withAlpha(alpha: Int): Int {
    return Color.argb(0x4D, Color.red(this), Color.green(this), Color.blue(this))
}