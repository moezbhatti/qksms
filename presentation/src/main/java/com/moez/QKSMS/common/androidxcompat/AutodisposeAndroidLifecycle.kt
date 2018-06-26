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
package com.moez.QKSMS.common.androidxcompat

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleOwner
import com.uber.autodispose.LifecycleScopeProvider
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.functions.Function

/**
 * Extension that returns a [LifecycleScopeProvider] for this [LifecycleOwner].
 */
@CheckReturnValue
inline fun LifecycleOwner.scope(): LifecycleScopeProvider<*> = AndroidLifecycleScopeProvider.from(this)

/**
 * Extension that returns a [LifecycleScopeProvider] for this [LifecycleOwner].
 *
 * @param untilEvent the event until the scope is valid.
 */
@CheckReturnValue
inline fun LifecycleOwner.scope(untilEvent: Lifecycle.Event): LifecycleScopeProvider<*>
        = AndroidLifecycleScopeProvider.from(this, untilEvent)

/**
 * Extension that returns a [LifecycleScopeProvider] for this [LifecycleOwner].
 *
 * @param boundaryResolver function that resolves the event boundary.
 */
@CheckReturnValue
inline fun LifecycleOwner.scope(boundaryResolver: Function<Event, Event>): LifecycleScopeProvider<*>
        = AndroidLifecycleScopeProvider.from(this, boundaryResolver)

/**
 * Extension that returns a [LifecycleScopeProvider] for this [Lifecycle].
 */
@CheckReturnValue
inline fun Lifecycle.scope(): LifecycleScopeProvider<*> = AndroidLifecycleScopeProvider.from(this)

/**
 * Extension that returns a [LifecycleScopeProvider] for this [Lifecycle].
 *
 * @param untilEvent the event until the scope is valid.
 */
@CheckReturnValue
inline fun Lifecycle.scope(untilEvent: Lifecycle.Event): LifecycleScopeProvider<*>
        = AndroidLifecycleScopeProvider.from(this, untilEvent)

/**
 * Extension that returns a [LifecycleScopeProvider] for this [Lifecycle].
 *
 * @param boundaryResolver function that resolves the event boundary.
 */
@CheckReturnValue
inline fun Lifecycle.scope(boundaryResolver: Function<Event, Event>): LifecycleScopeProvider<*>
        = AndroidLifecycleScopeProvider.from(this, boundaryResolver)