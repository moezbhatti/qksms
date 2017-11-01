package com.moez.QKSMS.presentation.conversations

import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.MarkAllSeen
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.base.QkViewModel
import io.reactivex.rxkotlin.plusAssign
import io.realm.RealmResults
import javax.inject.Inject

class ConversationsViewModel : QkViewModel<ConversationsView, ConversationsState>(ConversationsState()) {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var markAllSeen: MarkAllSeen

    private val conversations: RealmResults<Message>

    init {
        AppComponentManager.appComponent.inject(this)

        disposables += markAllSeen.disposables

        conversations = messageRepo.getConversationMessagesAsync()
        newState { it.copy(conversations = conversations) }

        markAllSeen.execute(Unit)
    }

    override fun bindIntents(view: ConversationsView) {
        super.bindIntents(view)

        intents += view.composeIntent.subscribe {
            newState { it.copy(drawerOpen = false) }
        }

        intents += view.drawerOpenIntent.filter { it }.subscribe {
            newState { it.copy(drawerOpen = true) }
        }

        intents += view.archivedIntent.subscribe {
            newState { it.copy(drawerOpen = false) }
        }

        intents += view.scheduledIntent.subscribe {
            newState { it.copy(drawerOpen = false) }
        }

        intents += view.blockedIntent.subscribe {
            newState { it.copy(drawerOpen = false) }
        }

        intents += view.settingsIntent.subscribe {
            navigator.showSettings()
            newState { it.copy(drawerOpen = false) }
        }
    }

}