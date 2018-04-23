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
package mapper

import com.f2prateek.rx.preferences2.RxSharedPreferences
import io.reactivex.rxkotlin.Observables
import manager.AnalyticsManager
import manager.RatingManager
import javax.inject.Inject

class RatingManagerImpl @Inject constructor(
        rxPrefs: RxSharedPreferences,
        private val analyticsManager: AnalyticsManager
) : RatingManager {

    companion object {
        private const val SESSION_THRESHOLD = 100
    }

    private val sessions = rxPrefs.getInteger("sessions", 0)
    private val rated = rxPrefs.getBoolean("rated", false)
    private val dismissed = rxPrefs.getBoolean("dismissed", false)

    override val shouldShowRating = Observables.combineLatest(
            sessions.asObservable(),
            rated.asObservable(),
            dismissed.asObservable(), { sessions, rated, dismissed ->

        sessions > SESSION_THRESHOLD && !rated && !dismissed
    })

    override fun addSession() {
        sessions.set(sessions.get() + 1)
    }

    override fun rate() {
        analyticsManager.track("Clicked Rate")
        rated.set(true)
    }

    override fun dismiss() {
        analyticsManager.track("Clicked Rate (Dismiss)")
        dismissed.set(true)
    }

}