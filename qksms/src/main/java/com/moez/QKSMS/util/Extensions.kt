package com.moez.QKSMS.util

import android.database.Cursor

fun Cursor.forEach(closeOnComplete: Boolean = true, method: (Cursor) -> Unit = {}) {
    moveToPosition(-1)
    while (moveToNext()) {
        method.invoke(this)
    }

    if (closeOnComplete) {
        close()
    }
}