package com.moez.QKSMS.presentation.defaultsms

import com.moez.QKSMS.presentation.common.base.QkViewModel
import io.reactivex.rxkotlin.plusAssign

class DefaultSmsViewModel : QkViewModel<DefaultSmsView, DefaultSmsState>(DefaultSmsState()) {

    override fun bindView(view: DefaultSmsView) {
        super.bindView(view)

        intents += view.skipIntent
                .subscribe { newState { it.copy(finished = true) } }

        intents += view.nextIntent
                .subscribe { newState { it.copy(requestPermission = true) } }

        intents += view.defaultSmsSetIntent
                .subscribe { isDefault -> newState { it.copy(requestPermission = false, finished = isDefault) } }
    }

}