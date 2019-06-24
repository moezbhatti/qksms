package com.moez.QKSMS.feature.blocking.manager

import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.util.Preferences
import io.reactivex.Observable
import kotlinx.android.synthetic.main.blocking_manager_controller.*
import kotlinx.android.synthetic.main.radio_preference_view.view.*
import javax.inject.Inject

class BlockingManagerController : QkController<BlockingManagerView, BlockingManagerState, BlockingManagerPresenter>(),
    BlockingManagerView {

    @Inject override lateinit var presenter: BlockingManagerPresenter

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

    override fun render(state: BlockingManagerState) {
        qksms.radioButton.isChecked = state.blockingManager == Preferences.BLOCKING_MANAGER_QKSMS
        callControl.radioButton.isChecked = state.blockingManager == Preferences.BLOCKING_MANAGER_CC
        shouldIAnswer.radioButton.isChecked = state.blockingManager == Preferences.BLOCKING_MANAGER_SIA
    }

    override fun qksmsClicked(): Observable<*> = qksms.clicks()
    override fun callControlClicked(): Observable<*> = callControl.clicks()
    override fun siaClicked(): Observable<*> = shouldIAnswer.clicks()

}
