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
package com.moez.QKSMS.extensions

import android.database.Cursor
import io.reactivex.Flowable

fun Cursor.forEach(closeOnComplete: Boolean = true, method: (Cursor) -> Unit = {}) {
    moveToPosition(-1)
    while (moveToNext()) {
        method.invoke(this)
    }

    if (closeOnComplete) {
        close()
    }
}

fun <T> Cursor.map(map: (Cursor) -> T): List<T> {
    return List(count) { position ->
        moveToPosition(position)
        map(this)
    }
}

/**
 * We're using this simple implementation with .range() because of the
 * complexities of dealing with Backpressure with a Cursor. We can't simply
 * use a loop and call onNext() from a generator because we'll need to close
 * the cursor at the end, and if any items are still in the buffer, then
 * they will be made invalid
 */
fun Cursor.asFlowable(): Flowable<Cursor> {
    return Flowable.range(0, count)
            .map {
                moveToPosition(it)
                this
            }
            .doOnComplete { close() }
}

/**
 * Dumps the contents of the cursor as a CSV string
 */
fun Cursor.dump(): String {
    val lines = mutableListOf<String>()

    lines += columnNames.joinToString(",")
    forEach { lines += (0 until columnCount).joinToString(",", transform = ::getString) }

    return lines.joinToString("\n")
}
