package com.moez.QKSMS.presentation.setup

import android.Manifest
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.common.base.QkActivity
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.setup_activity.*
import javax.inject.Inject

class SetupActivity : QkActivity<SetupViewModel>(), SetupView {

    override val viewModelClass = SetupViewModel::class
    override val activityResumedIntent: Subject<Unit> = PublishSubject.create()
    override val skipIntent by lazy { skip.clicks() }
    override val nextIntent by lazy { next.clicks() }

    @Inject lateinit var navigator: Navigator

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setup_activity)
        viewModel.bindView(this)

        disposables += colors.background
                .subscribe { color -> window.decorView.setBackgroundColor(color) }
    }

    override fun onResume() {
        super.onResume()
        activityResumedIntent.onNext(Unit)
    }

    override fun render(state: SetupState) {
    }

    override fun requestDefaultSms() {
        navigator.showDefaultSmsDialog()
    }

    override fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS), 0)
    }

}