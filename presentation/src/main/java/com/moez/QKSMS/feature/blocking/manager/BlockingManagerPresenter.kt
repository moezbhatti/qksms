package com.moez.QKSMS.feature.blocking.manager

import android.os.Build
import com.moez.QKSMS.blocking.AndroidBlockingClient
import com.moez.QKSMS.blocking.CallControlBlockingClient
import com.moez.QKSMS.blocking.ShouldIAnswerBlockingClient
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.base.QkPresenter
import com.moez.QKSMS.manager.AnalyticsManager
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class BlockingManagerPresenter @Inject constructor(
    private val analytics: AnalyticsManager,
    private val android: AndroidBlockingClient,
    private val callControl: CallControlBlockingClient,
    private val navigator: Navigator,
    private val prefs: Preferences,
    private val shouldIAnswer: ShouldIAnswerBlockingClient
) : QkPresenter<BlockingManagerView, BlockingManagerState>(BlockingManagerState(
        blockingManager = prefs.blockingManager.get(),
        callControlInstalled = callControl.isAvailable(),
        siaInstalled = shouldIAnswer.isAvailable()
)) {

    init {
        disposables += prefs.blockingManager.asObservable()
                .subscribe { manager -> newState { copy(blockingManager = manager) } }
    }

    override fun bindIntents(view: BlockingManagerView) {
        super.bindIntents(view)

        view.activityResumed()
                .map { callControl.isAvailable() }
                .distinctUntilChanged()
                .autoDisposable(view.scope())
                .subscribe { available -> newState { copy(callControlInstalled = available) } }

        view.activityResumed()
                .map { shouldIAnswer.isAvailable() }
                .distinctUntilChanged()
                .autoDisposable(view.scope())
                .subscribe { available -> newState { copy(siaInstalled = available) } }

        view.androidClicked()
                .autoDisposable(view.scope())
                .subscribe {
                    analytics.setUserProperty("Blocking Manager", "Android")
                    prefs.blockingManager.set(Preferences.BLOCKING_MANAGER_ANDROID)
                }

        view.launchAndroidClicked()
                .autoDisposable(view.scope())
                .subscribe {
                    // TODO: This is a hack, get rid of it once we implement AndroidX navigation
                    if (Build.VERSION.SDK_INT < 24) {
                        view.openBlockedNumbers()
                    } else {
                        android.openSettings()
                    }
                }

        view.callControlClicked()
                .filter {
                    val installed = callControl.isAvailable()
                    if (!installed) {
                        analytics.track("Install Call Control")
                        navigator.installCallControl()
                    }

                    val enabled = prefs.blockingManager.get() == Preferences.BLOCKING_MANAGER_CC
                    installed && !enabled
                }
                .autoDisposable(view.scope())
                .subscribe {
                    callControl.isBlocked("")
                    analytics.setUserProperty("Blocking Manager", "Call Control")
                    prefs.blockingManager.set(Preferences.BLOCKING_MANAGER_CC)
                }

        view.launchCallControlClicked()
                .autoDisposable(view.scope())
                .subscribe {
                    when (callControl.isAvailable()) {
                        true -> callControl.openSettings()
                        false -> navigator.installCallControl()
                    }
                }

        view.siaClicked()
                .filter {
                    val installed = shouldIAnswer.isAvailable()
                    if (!installed) {
                        analytics.track("Install SIA")
                        navigator.installSia()
                    }

                    val enabled = prefs.blockingManager.get() == Preferences.BLOCKING_MANAGER_SIA
                    installed && !enabled
                }
                .autoDisposable(view.scope())
                .subscribe {
                    analytics.setUserProperty("Blocking Manager", "SIA")
                    prefs.blockingManager.set(Preferences.BLOCKING_MANAGER_SIA)
                }

        view.launchSiaClicked()
                .autoDisposable(view.scope())
                .subscribe {
                    when (shouldIAnswer.isAvailable()) {
                        true -> shouldIAnswer.openSettings()
                        false -> navigator.installSia()
                    }
                }
    }

}
