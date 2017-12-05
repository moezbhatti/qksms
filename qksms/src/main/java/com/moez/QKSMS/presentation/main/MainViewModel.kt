package com.moez.QKSMS.presentation.main

import android.content.Context
import android.provider.Telephony
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.DeleteConversation
import com.moez.QKSMS.domain.interactor.MarkAllSeen
import com.moez.QKSMS.domain.interactor.MarkArchived
import com.moez.QKSMS.domain.interactor.PartialSync
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.base.QkViewModel
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import javax.inject.Inject

class MainViewModel : QkViewModel<MainView, MainState>(MainState(page = MainPage.INBOX)) {

    @Inject lateinit var context: Context
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var markAllSeen: MarkAllSeen
    @Inject lateinit var deleteConversation: DeleteConversation
    @Inject lateinit var markArchived: MarkArchived
    @Inject lateinit var partialSync: PartialSync

    init {
        appComponent.inject(this)

        disposables += markAllSeen

        disposables += messageRepo.getConversations().subscribe { conversations ->
            newState { it.copy(conversations = conversations) }
        }

        disposables += messageRepo.getConversations(true).subscribe { conversations ->
            newState { it.copy(archivedConversations = conversations) }
        }

        if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
            partialSync.execute(Unit)
        }

        markAllSeen.execute(Unit)
    }

    override fun bindView(view: MainView) {
        super.bindView(view)

        intents += view.composeIntent.subscribe {
            navigator.showCompose()
            newState { it.copy(drawerOpen = false) }
        }

        intents += view.drawerOpenIntent.filter { it }.subscribe {
            newState { it.copy(drawerOpen = true) }
        }

        intents += view.inboxIntent.subscribe {
            newState { it.copy(page = MainPage.INBOX, drawerOpen = false) }
        }

        intents += view.archivedIntent.subscribe {
            newState { it.copy(page = MainPage.ARCHIVED, drawerOpen = false) }
        }

        intents += view.scheduledIntent.subscribe {
            newState { it.copy(page = MainPage.SCHEDULED, drawerOpen = false) }
        }

        intents += view.blockedIntent.subscribe {
            newState { it.copy(page = MainPage.BLOCKED, drawerOpen = false) }
        }

        intents += view.settingsIntent.subscribe {
            navigator.showSettings()
            newState { it.copy(drawerOpen = false) }
        }

        intents += view.deleteConversationIntent
                .subscribe { threadId -> deleteConversation.execute(threadId) }

        intents += view.archiveConversationIntent
                .withLatestFrom(state, { position, state -> state.conversations[position] })
                .map { pair -> pair.conversation.id }
                .subscribe { threadId -> markArchived.execute(threadId) }
    }

}