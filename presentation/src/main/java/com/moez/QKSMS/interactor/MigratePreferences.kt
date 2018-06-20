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
package com.moez.QKSMS.interactor

import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.moez.QKSMS.util.NightModeManager
import com.moez.QKSMS.util.Preferences
import io.reactivex.Flowable
import javax.inject.Inject

/**
 * When upgrading from 2.7.3 to 3.0, migrate the preferences
 *
 * Blocked conversations will be migrated in SyncManager
 */
class MigratePreferences @Inject constructor(
        private val nightModeManager: NightModeManager,
        private val prefs: Preferences,
        private val rxPrefs: RxSharedPreferences
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit): Flowable<*> {
        return Flowable.just(rxPrefs.getBoolean("pref_key_welcome_seen", false))
                .filter { seen -> seen.get() } // Only proceed if this value is true. It will be set false at the end
                .doOnNext {
                    // Theme
                    val defaultTheme = prefs.theme().get().toString()
                    val oldTheme = rxPrefs.getString("pref_key_theme", defaultTheme).get()
                    prefs.theme().set(Integer.parseInt(oldTheme))

                    // Night mode
                    val background = rxPrefs.getString("pref_key_background", "light").get()
                    val autoNight = rxPrefs.getBoolean("pref_key_night_auto", false).get()
                    when {
                        autoNight -> nightModeManager.updateNightMode(Preferences.NIGHT_MODE_AUTO)
                        background == "light" -> nightModeManager.updateNightMode(Preferences.NIGHT_MODE_OFF)
                        background == "grey" -> nightModeManager.updateNightMode(Preferences.NIGHT_MODE_ON)
                        background == "black" -> {
                            nightModeManager.updateNightMode(Preferences.NIGHT_MODE_ON)
                            prefs.black.set(true)
                        }
                    }

                    // Delivery
                    prefs.delivery.set(rxPrefs.getBoolean("pref_key_delivery", prefs.delivery.get()).get())

                    // Quickreply
                    prefs.qkreply.set(rxPrefs.getBoolean("pref_key_quickreply_enabled", prefs.qkreply.get()).get())
                    prefs.qkreplyTapDismiss.set(rxPrefs.getBoolean("pref_key_quickreply_dismiss", prefs.qkreplyTapDismiss.get()).get())

                    // Font size
                    prefs.textSize.set(rxPrefs.getString("pref_key_font_size", "${prefs.textSize.get()}").get().toInt())

                    // Unicode
                    prefs.unicode.set(rxPrefs.getBoolean("pref_key_strip_unicode", prefs.unicode.get()).get())
                }
                .doOnNext { seen -> seen.delete() } // Clear this value so that we don't need to migrate again
    }

}