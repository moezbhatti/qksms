package com.moez.QKSMS.util.extensions

import android.content.Context
import android.support.v4.content.ContextCompat

fun Context.getColorCompat(colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}