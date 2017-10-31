package com.moez.QKSMS.presentation.conversations

import com.moez.QKSMS.presentation.base.QkView
import io.reactivex.Observable

interface ConversationsView : QkView<ConversationsState> {

    val composeIntent: Observable<Unit>
    val archivedIntent: Observable<Unit>
    val scheduledIntent: Observable<Unit>
    val blockedIntent: Observable<Unit>
    val settingsIntent: Observable<Unit>

}