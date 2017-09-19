package com.moez.QKSMS.util

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.database.Cursor
import io.reactivex.Flowable
import java.util.*

fun Calendar.isSameDay(other: Calendar): Boolean {
    return get(Calendar.YEAR) == other.get(Calendar.YEAR) && get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}

fun Calendar.isSameWeek(other: Calendar): Boolean {
    return get(Calendar.YEAR) == other.get(Calendar.YEAR) && get(Calendar.WEEK_OF_YEAR) == other.get(Calendar.WEEK_OF_YEAR)
}

fun Calendar.isSameYear(other: Calendar): Boolean {
    return get(Calendar.YEAR) == other.get(Calendar.YEAR)
}

fun Calendar.isDayAfter(other: Calendar): Boolean {
    other.add(Calendar.DAY_OF_YEAR, 1)
    return isSameDay(other)
}

fun Cursor.forEach(closeOnComplete: Boolean = true, method: (Cursor) -> Unit = {}) {
    moveToPosition(-1)
    while (moveToNext()) {
        method.invoke(this)
    }

    if (closeOnComplete) {
        close()
    }
}


fun <T, O> LiveData<T>.observe(observer: O) where O : LifecycleOwner, O : Observer<T> {
    observe(observer, observer)
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

