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
package com.moez.QKSMS.manager

interface ChangelogManager {

    data class CumulativeChangelog(
        val added: List<String>,
        val improved: List<String>,
        val fixed: List<String>
    )

    /**
     * Returns true if the app has benn updated since the last time this method was called
     */
    fun didUpdate(): Boolean

    suspend fun getChangelog(): CumulativeChangelog

    fun markChangelogSeen()

}
