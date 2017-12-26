package com.moez.QKSMS.presentation.setup

import android.content.Context
import android.provider.Telephony
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.Permissions
import com.moez.QKSMS.presentation.common.base.QkViewModel
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class SetupViewModel : QkViewModel<SetupView, SetupState>(SetupState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var permissions: Permissions

    init {
        appComponent.inject(this)
    }

    override fun bindView(view: SetupView) {
        super.bindView(view)

        intents += view.activityResumedIntent
                .subscribe {
                    val isDefault = Telephony.Sms.getDefaultSmsPackage(context) == context.packageName

                    if (!permissions.hasSmsAndContacts()) view.requestPermissions()
                    else if (isDefault) view.finish()
                }

        intents += view.skipIntent.subscribe { view.finish() }

        intents += view.nextIntent.subscribe { view.requestDefaultSms() }
    }

}