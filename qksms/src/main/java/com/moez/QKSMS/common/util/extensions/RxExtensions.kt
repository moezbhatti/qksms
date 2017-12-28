package com.moez.QKSMS.common.util.extensions

import com.moez.QKSMS.common.util.Optional
import io.reactivex.Flowable

fun <T, R> Flowable<T>.mapNotNull(mapper: (T) -> R?): Flowable<R>
        = map { input -> Optional(mapper(input)) }
        .filter { optional -> optional.notNull() }
        .map { optional -> optional.value }