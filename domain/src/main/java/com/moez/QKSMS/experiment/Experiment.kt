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
package com.moez.QKSMS.experiment

import android.content.Context
import android.preference.PreferenceManager
import com.moez.QKSMS.manager.AnalyticsManager
import java.util.*

abstract class Experiment<T>(val context: Context, val analytics: AnalyticsManager) {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    private val prefKey: String
        get() = "experiment_$key"

    protected abstract val key: String
    protected abstract val variants: List<Variant<T>>
    protected abstract val default: T

    /**
     * Returns true if the current device qualifies for the experiment
     */
    protected open val qualifies: Boolean by lazy { Locale.getDefault().language.startsWith("en") }

    val variant: T by lazy {
        when {
            !qualifies -> null // Device doesn't quality for experiment

            prefs.contains(prefKey) -> { // Variant already set
                variants.firstOrNull { it.key == prefs.getString(prefKey, null) }?.value
            }

            else -> { // Variant hasn't been set yet
                variants[Random().nextInt(variants.size)].also { variant ->
                    analytics.setUserProperty("Experiment: $key", variant.key)
                    prefs.edit().putString(prefKey, variant.key).apply()
                }.value
            }
        } ?: default
    }

}