package com.moez.QKSMS.common.util

import android.content.Context
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.moez.QKSMS.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Analytics @Inject constructor(context: Context) {

    private val amplitude: AmplitudeClient = Amplitude.getInstance().initialize(context, BuildConfig.AMPLITUDE_API_KEY)
    private val mixpanel: MixpanelAPI = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_API_KEY)

    fun track(event: String) {
        amplitude.logEvent(event)

        synchronized(mixpanel, {
            mixpanel.track(event)
        })
    }

}