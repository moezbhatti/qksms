package com.moez.QKSMS.presentation.messages

import com.moez.QKSMS.presentation.base.QkView
import io.reactivex.Observable

interface MessagesView : QkView<MessagesState> {

    val textChangedIntent: Observable<CharSequence>
    val attachIntent: Observable<Unit>
    val sendIntent: Observable<Unit>

}