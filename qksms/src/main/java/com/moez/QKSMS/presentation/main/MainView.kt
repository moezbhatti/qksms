package com.moez.QKSMS.presentation.main

import com.moez.QKSMS.presentation.common.base.QkView
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface MainView : QkView<MainState> {

    val queryChangedIntent: Observable<CharSequence>
    val composeIntent: Observable<Unit>
    val drawerOpenIntent: Observable<Boolean>
    val drawerItemIntent: Observable<DrawerItem>
    val conversationClickIntent: Observable<Long>
    val conversationLongClickIntent: Observable<Long>
    val conversationMenuItemIntent: Subject<Int>
    val swipeConversationIntent: Observable<Int>
    val undoSwipeConversationIntent: Subject<Unit>

}

enum class DrawerItem { INBOX, ARCHIVED, SCHEDULED, BLOCKED, SETTINGS }