package com.moez.QKSMS.presentation.defaultsms

import com.moez.QKSMS.presentation.common.base.QkView
import io.reactivex.Observable

interface DefaultSmsView : QkView<DefaultSmsState> {

    val skipIntent: Observable<*>
    val nextIntent: Observable<*>
    val defaultSmsSetIntent: Observable<Boolean>

}