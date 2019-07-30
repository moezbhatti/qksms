package com.moez.QKSMS.feature.blocking.manager

import android.app.Activity
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkChangeHandler
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.feature.blocking.numbers.BlockedNumbersController
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.util.Preferences
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.blocking_manager_controller.*
import kotlinx.android.synthetic.main.blocking_manager_list_option.view.*
import kotlinx.android.synthetic.main.radio_preference_view.view.*
import javax.inject.Inject

class BlockingManagerController : QkController<BlockingManagerView, BlockingManagerState, BlockingManagerPresenter>(),
    BlockingManagerView {

    @Inject override lateinit var presenter: BlockingManagerPresenter

    private val activityResumedSubject: PublishSubject<Unit> = PublishSubject.create()

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.blocking_manager_controller
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.blocking_manager_title)
        showBackButton(true)
    }

    override fun onActivityResumed(activity: Activity) {
        activityResumedSubject.onNext(Unit)
    }

    override fun render(state: BlockingManagerState) {
        android.radioButton.isChecked = state.blockingManager == Preferences.BLOCKING_MANAGER_ANDROID

        callControl.radioButton.isChecked = state.blockingManager == Preferences.BLOCKING_MANAGER_CC
        callControl.launch.setImageResource(when (state.callControlInstalled) {
            true -> R.drawable.ic_chevron_right_black_24dp
            false -> R.drawable.ic_info_black_24dp
        })

        shouldIAnswer.radioButton.isChecked = state.blockingManager == Preferences.BLOCKING_MANAGER_SIA
        shouldIAnswer.launch.setImageResource(when (state.siaInstalled) {
            true -> R.drawable.ic_chevron_right_black_24dp
            false -> R.drawable.ic_info_black_24dp
        })
    }

    override fun activityResumed(): Observable<*> = activityResumedSubject
    override fun androidClicked(): Observable<*> = android.clicks()
    override fun launchAndroidClicked(): Observable<*> = android.launch.clicks()
    override fun callControlClicked(): Observable<*> = callControl.clicks()
    override fun launchCallControlClicked(): Observable<*> = callControl.launch.clicks()
    override fun siaClicked(): Observable<*> = shouldIAnswer.clicks()
    override fun launchSiaClicked(): Observable<*> = shouldIAnswer.launch.clicks()

    override fun openBlockedNumbers() {
        router.pushController(RouterTransaction.with(BlockedNumbersController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

}
