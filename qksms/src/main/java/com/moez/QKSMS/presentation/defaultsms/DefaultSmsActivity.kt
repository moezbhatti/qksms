package com.moez.QKSMS.presentation.defaultsms

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.presentation.common.base.QkActivity
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.default_sms_activity.*

class DefaultSmsActivity : QkActivity<DefaultSmsViewModel>(), DefaultSmsView {

    override val viewModelClass = DefaultSmsViewModel::class
    override val skipIntent by lazy { skip.clicks() }
    override val nextIntent by lazy { next.clicks() }
    override val defaultSmsSetIntent: Subject<Boolean> = PublishSubject.create()

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.default_sms_activity)
        viewModel.bindView(this)

        disposables += colors.background
                .subscribe { color -> window.decorView.setBackgroundColor(color) }
    }

    override fun onResume() {
        super.onResume()
        defaultSmsSetIntent.onNext(Telephony.Sms.getDefaultSmsPackage(this) == packageName)
    }

    override fun render(state: DefaultSmsState) {
        if (state.requestPermission) {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            startActivity(intent)
        }

        if (state.finished) {
            finish()
        }
    }

}