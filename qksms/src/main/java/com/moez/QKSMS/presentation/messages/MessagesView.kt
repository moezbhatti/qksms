package com.moez.QKSMS.presentation.messages

import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.presentation.base.QkView
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface MessagesView : QkView<MessagesState> {

    val copyTextIntent: Subject<Message>
    val forwardMessageIntent: Subject<Message>
    val deleteMessageIntent: Subject<Message>
    val textChangedIntent: Observable<CharSequence>
    val attachIntent: Observable<Unit>
    val sendIntent: Observable<Unit>

}