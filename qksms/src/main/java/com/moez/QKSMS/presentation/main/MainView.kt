package com.moez.QKSMS.presentation.main

import com.moez.QKSMS.presentation.base.QkView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface MainView : QkView<MainState> {

    val composeIntent: Observable<Unit>
    val drawerOpenIntent: Observable<Boolean>
    val inboxIntent: Observable<Unit>
    val archivedIntent: Observable<Unit>
    val scheduledIntent: Observable<Unit>
    val blockedIntent: Observable<Unit>
    val settingsIntent: Observable<Unit>
    val conversationSwipedIntent: PublishSubject<Int>

}