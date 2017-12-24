package com.moez.QKSMS.common.util.extensions

import android.database.Cursor
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.subjects.MaybeSubject

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
    return List(count, { position ->
        moveToPosition(position)
        map(this)
    })
}

fun <T> Cursor.mapWhile(map: (Cursor) -> T, predicate: (T) -> Boolean): ArrayList<T> {
    val result = ArrayList<T>()

    moveToPosition(-1)
    while (moveToNext()) {
        val item = map(this)

        if (!predicate(item)) break

        result.add(item)
    }

    return result
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

fun Cursor.asMaybe(): Maybe<Cursor> {
    val subject = MaybeSubject.create<Cursor>()

    if (moveToFirst()) {
        subject.onSuccess(this)
    } else {
        subject.onError(IndexOutOfBoundsException("The cursor has no items"))
    }

    subject.doOnComplete { close() }
    return subject
}



