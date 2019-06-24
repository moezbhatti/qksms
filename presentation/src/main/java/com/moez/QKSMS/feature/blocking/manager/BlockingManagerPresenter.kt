package com.moez.QKSMS.feature.blocking.manager

import android.content.Context
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.base.QkPresenter
import com.moez.QKSMS.common.util.extensions.isInstalled
import com.moez.QKSMS.manager.AnalyticsManager
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class BlockingManagerPresenter @Inject constructor(
    private val analytics: AnalyticsManager,
    private val context: Context,
    private val navigator: Navigator,
    private val prefs: Preferences
) : QkPresenter<BlockingManagerView, BlockingManagerState>(BlockingManagerState()) {

    init {
        disposables += prefs.blockingManager.asObservable()
                .subscribe { manager -> newState { copy(blockingManager = manager) } }
    }

    override fun bindIntents(view: BlockingManagerView) {
        super.bindIntents(view)

        view.qksmsClicked()
                .autoDisposable(view.scope())
                .subscribe { prefs.blockingManager.set(Preferences.BLOCKING_MANAGER_QKSMS) }

        view.callControlClicked()
                .filter {
                    val installed = context.isInstalled("com.flexaspect.android.everycallcontrol")
                    if (!installed) {
                        navigator.showCallControl()
                    }

                    val enabled = prefs.blockingManager.get() == Preferences.BLOCKING_MANAGER_CC
                    analytics.track("Clicked Call Control", Pair("enable", !enabled), Pair("installed", installed))
                    installed
                }
                .autoDisposable(view.scope())
                .subscribe { prefs.blockingManager.set(Preferences.BLOCKING_MANAGER_CC) }

        view.siaClicked()
                .filter {
                    val installed = listOf("org.mistergroup.shouldianswer",
                            "org.mistergroup.shouldianswerpersonal",
                            "org.mistergroup.muzutozvednout")
                            .any(context::isInstalled)

                    if (!installed) {
                        navigator.showSia()
                    }

                    val enabled = prefs.blockingManager.get() == Preferences.BLOCKING_MANAGER_SIA
                    analytics.track("Clicked SIA", Pair("enable", !enabled), Pair("installed", installed))
                    installed && !enabled
                }
                .autoDisposable(view.scope())
                .subscribe { prefs.blockingManager.set(Preferences.BLOCKING_MANAGER_SIA) }
    }

}
