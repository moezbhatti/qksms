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
package com.moez.QKSMS.blocking

import io.reactivex.Completable
import io.reactivex.Single

interface BlockingClient {

    /**
     * Returns true if the target blocking client is available for use, ie. it is installed
     */
    fun isAvailable(): Boolean

    /**
     * Returns the level of access that the given blocking client provides to QKSMS
     */
    fun getClientCapability(): BlockingClientCapability

    /**
     * Return a Single<Boolean> which emits whether or not the given [address] should be blocked
     */
    fun isBlocked(address: String): Single<Boolean>

    /**
     * Blocks the numbers or opens the manager
     */
    fun block(addresses: List<String>): Completable

    /**
     * Unblocks the numbers or opens the manager
     */
    fun unblock(addresses: List<String>): Completable

    /**
     * Opens the settings page for the blocking manager
     */
    fun openSettings()

}
