package com.moez.QKSMS.presentation.compose

import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.presentation.base.QkView
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface ComposeView : QkView<ComposeState> {

    val queryChangedIntent: Observable<CharSequence>
    val chipSelectedIntent: Subject<Contact>
    val chipDeletedIntent: Subject<Contact>
    val callIntent: Subject<Unit>
    val archiveIntent: Subject<Unit>
    val copyTextIntent: Subject<Message>
    val forwardMessageIntent: Subject<Message>
    val deleteMessageIntent: Subject<Message>
    val textChangedIntent: Observable<CharSequence>
    val attachIntent: Observable<Unit>
    val sendIntent: Observable<Unit>

}