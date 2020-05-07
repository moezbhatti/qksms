package com.moez.QKSMS.manager

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.moez.QKSMS.util.Preferences
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class ReferralManagerImpl @Inject constructor(
    private val analytics: AnalyticsManager,
    private val context: Context,
    private val prefs: Preferences
) : ReferralManager {

    override suspend fun trackReferrer() {
        if (prefs.didSetReferrer.get()) {
            return
        }

        context.packageManager.getInstallerPackageName(context.packageName)?.let { installer ->
            analytics.setUserProperty("Installer", installer)
        }

        val referrerClient = InstallReferrerClient.newBuilder(context).build()
        val responseCode = suspendCancellableCoroutine<Int> { cont ->
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    cont.resume(responseCode)
                }

                override fun onInstallReferrerServiceDisconnected() {
                    cont.resume(InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED)
                }
            })

            cont.invokeOnCancellation {
                referrerClient.endConnection()
            }
        }

        when (responseCode) {
            InstallReferrerClient.InstallReferrerResponse.OK -> {
                analytics.setUserProperty("Referrer", referrerClient.installReferrer.installReferrer)
                prefs.didSetReferrer.set(true)
            }

            InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                prefs.didSetReferrer.set(true)
            }
        }

        referrerClient.endConnection()
    }

}
