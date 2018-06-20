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

import io.reactivex.Flowable
import io.reactivex.Observable

data class Optional<out T>(val value: T?) {
    fun notNull() = value != null
}

fun <T, R> Flowable<T>.mapNotNull(mapper: (T) -> R?): Flowable<R>
        = map { input -> Optional(mapper(input)) }
        .filter { optional -> optional.notNull() }
        .map { optional -> optional.value }

fun <T, R> Observable<T>.mapNotNull(mapper: (T) -> R?): Observable<R>
        = map { input -> Optional(mapper(input)) }
        .filter { optional -> optional.notNull() }
        .map { optional -> optional.value }

fun <T> Observable<T>.toFlowable(): Flowable<T> = this.toFlowable(io.reactivex.BackpressureStrategy.BUFFER)