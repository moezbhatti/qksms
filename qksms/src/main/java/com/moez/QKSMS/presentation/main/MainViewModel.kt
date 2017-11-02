package com.moez.QKSMS.presentation.main

import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.MarkAllSeen
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.base.QkViewModel
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class MainViewModel : QkViewModel<MainView, MainState>(MainState()) {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var markAllSeen: MarkAllSeen

    init {
        AppComponentManager.appComponent.inject(this)

        disposables += markAllSeen

        newState { it.copy(page = MainPage.INBOX, adapter = ConversationsAdapter(messageRepo.getConversationMessagesAsync())) }

        markAllSeen.execute(Unit)
    }

    override fun bindIntents(view: MainView) {
        super.bindIntents(view)

        intents += view.composeIntent.subscribe {
            newState { it.copy(drawerOpen = false) }
        }

        intents += view.drawerOpenIntent.filter { it }.subscribe {
            newState { it.copy(drawerOpen = true) }
        }

        intents += view.inboxIntent.subscribe {
            newState {
                it.copy(
                        page = MainPage.INBOX,
                        adapter = ConversationsAdapter(messageRepo.getConversationMessagesAsync()),
                        drawerOpen = false)
            }
        }

        intents += view.archivedIntent.subscribe {
            newState {
                it.copy(
                        page = MainPage.ARCHIVED,
                        adapter = null,
                        drawerOpen = false)
            }
        }

        intents += view.scheduledIntent.subscribe {
            newState {
                it.copy(
                        page = MainPage.SCHEDULED,
                        adapter = null,
                        drawerOpen = false)
            }
        }

        intents += view.blockedIntent.subscribe {
            newState {
                it.copy(
                        page = MainPage.BLOCKED,
                        adapter = null,
                        drawerOpen = false)
            }
        }

        intents += view.settingsIntent.subscribe {
            navigator.showSettings()
            newState { it.copy(drawerOpen = false) }
        }
    }

}