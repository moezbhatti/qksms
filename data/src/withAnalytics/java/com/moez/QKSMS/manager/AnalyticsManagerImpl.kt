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

import android.content.Context
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.amplitude.api.Identify
import com.moez.QKSMS.data.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManagerImpl @Inject constructor(context: Context) : AnalyticsManager {

    private val amplitude: AmplitudeClient = Amplitude.getInstance().initialize(context, BuildConfig.AMPLITUDE_API_KEY)

    init {
        amplitude.trackSessionEvents(true)
    }

    override fun track(event: String, vararg properties: Pair<String, Any>) {
        val propertiesJson = JSONObject(properties
                .associateBy { pair -> pair.first }
                .mapValues { pair -> pair.value.second })
                .also { Timber.v("$event: $it") }

        amplitude.logEvent(event, propertiesJson)
    }

    override fun setUserProperty(key: String, value: Any) {
        Timber.v("$key: $value")

        // Set the value in Amplitude
        val identify = Identify()
        when (value) {
            is Boolean -> identify.set(key, value)
            is BooleanArray -> identify.set(key, value)
            is Double -> identify.set(key, value)
            is DoubleArray -> identify.set(key, value)
            is Float -> identify.set(key, value)
            is FloatArray -> identify.set(key, value)
            is Int -> identify.set(key, value)
            is IntArray -> identify.set(key, value)
            is Long -> identify.set(key, value)
            is LongArray -> identify.set(key, value)
            is String -> identify.set(key, value)
            is JSONArray -> identify.set(key, value)
            is JSONObject -> identify.set(key, value)
            else -> Timber.e("Value of type ${value::class.java} not supported")
        }
        amplitude.identify(identify)
    }

}
