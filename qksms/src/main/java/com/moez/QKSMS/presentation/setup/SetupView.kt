package com.moez.QKSMS.presentation.setup

import com.moez.QKSMS.presentation.common.base.QkView
import io.reactivex.Observable

interface SetupView : QkView<SetupState> {

    val activityResumedIntent: Observable<*>
    val skipIntent: Observable<*>
    val nextIntent: Observable<*>

    fun requestDefaultSms()
    fun requestPermissions()
    fun finish()

}