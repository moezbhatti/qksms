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
package common.util

import android.content.Context
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.moez.QKSMS.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsImpl @Inject constructor(context: Context): Analytics {

    private val amplitude: AmplitudeClient = Amplitude.getInstance().initialize(context, BuildConfig.AMPLITUDE_API_KEY)
    private val mixpanel: MixpanelAPI = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_API_KEY)

    override fun track(event: String) {
        amplitude.logEvent(event)

        synchronized(mixpanel, {
            mixpanel.track(event)
        })
    }

}
