package com.moez.QKSMS.presentation.compose

import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.presentation.base.QkView
import io.reactivex.subjects.Subject

interface ComposeView : QkView<ComposeState> {

    val queryChangedIntent: Subject<CharSequence>
    val chipSelectedIntent: Subject<Contact>
    val chipDeletedIntent: Subject<Contact>

}