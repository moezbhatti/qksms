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
package com.moez.QKSMS.domain.interactor

import com.moez.QKSMS.common.util.Keys
import com.moez.QKSMS.common.util.Permissions
import com.moez.QKSMS.common.util.SyncManager
import io.reactivex.Flowable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class PartialSync @Inject constructor(
        private val syncManager: SyncManager,
        private val permissions: Permissions,
        private val keys: Keys
) : Interactor<Unit, Long>() {

    override fun buildObservable(params: Unit): Flowable<Long> {
        return Flowable.just(System.currentTimeMillis())
                .skipWhile { !permissions.hasSmsAndContacts() }
                .doOnNext { syncManager.performSync() }
                .doOnNext { keys.refresh() }
                .map { startTime -> System.currentTimeMillis() - startTime }
                .map { elapsed -> TimeUnit.MILLISECONDS.toSeconds(elapsed) }
                .doOnNext { seconds -> Timber.v("Completed sync in $seconds seconds") }
    }

}