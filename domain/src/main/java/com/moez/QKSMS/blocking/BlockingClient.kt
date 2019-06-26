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
     * Return a Single<Boolean> which emits whether or not the given [address] should be blocked
     */
    fun isBlocked(address: String): Single<Boolean>

    /**
     * Returns true if the client is capable of blocking the address in the manager without any
     * further user input
     */
    fun canBlock(): Boolean = false

    /**
     * Blocks the numbers or opens the manager
     */
    fun block(addresses: List<String>): Completable

    /**
     * Returns true if the client is capable of unblocking the address in the manager without any
     * further user input
     */
    fun canUnblock(): Boolean = false

    /**
     * Unblocks the numbers or opens the manager
     */
    fun unblock(addresses: List<String>): Completable

    /**
     * Opens the settings page for the blocking manager
     */
    fun openSettings()

}