package com.moez.QKSMS.presentation.main

import android.content.Context
import android.provider.Telephony
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.MarkAllSeen
import com.moez.QKSMS.domain.interactor.MarkArchived
import com.moez.QKSMS.domain.interactor.PartialSync
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.base.QkViewModel
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class MainViewModel : QkViewModel<MainView, MainState>(MainState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var markAllSeen: MarkAllSeen
    @Inject lateinit var markArchived: MarkArchived
    @Inject lateinit var partialSync: PartialSync

    init {
        AppComponentManager.appComponent.inject(this)

        disposables += markAllSeen

        newState { it.copy(page = MainPage.INBOX, adapter = ConversationsAdapter(messageRepo.getConversations())) }

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
            val adapter = ConversationsAdapter(messageRepo.getConversations())
            newState { it.copy(page = MainPage.INBOX, adapter = adapter, drawerOpen = false) }
        }

        intents += view.archivedIntent.subscribe {
            val adapter = ConversationsAdapter(messageRepo.getConversations(true))
            newState { it.copy(page = MainPage.ARCHIVED, adapter = adapter, drawerOpen = false) }
        }

        intents += view.scheduledIntent.subscribe {
            newState { it.copy(page = MainPage.SCHEDULED, adapter = null, drawerOpen = false) }
        }

        intents += view.blockedIntent.subscribe {
            newState { it.copy(page = MainPage.BLOCKED, adapter = null, drawerOpen = false) }
        }

        intents += view.settingsIntent.subscribe {
            navigator.showSettings()
            newState { it.copy(drawerOpen = false) }
        }

        intents += view.conversationSwipedIntent.subscribe { adapterPosition ->
            state.value
                    ?.takeIf { state -> state.page == MainPage.INBOX }
                    ?.takeIf { state -> state.adapter is ConversationsAdapter }
                    ?.let { state ->
                        val adapter = state.adapter as ConversationsAdapter
                        val conversation = adapter.getItem(adapterPosition).conversation
                        markArchived.execute(conversation.id)
                    }
        }

    }

}