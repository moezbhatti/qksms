package com.moez.QKSMS.presentation.main

import android.content.Context
import android.provider.Telephony
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.filter.ConversationFilter
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.DeleteConversation
import com.moez.QKSMS.domain.interactor.MarkAllSeen
import com.moez.QKSMS.domain.interactor.MarkArchived
import com.moez.QKSMS.domain.interactor.PartialSync
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.common.base.QkViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel : QkViewModel<MainView, MainState>(MainState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var conversationFilter: ConversationFilter
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var markAllSeen: MarkAllSeen
    @Inject lateinit var deleteConversation: DeleteConversation
    @Inject lateinit var markArchived: MarkArchived
    @Inject lateinit var partialSync: PartialSync

    private val conversations by lazy { messageRepo.getConversations() }

    init {
        appComponent.inject(this)

        disposables += markAllSeen
        disposables += deleteConversation
        disposables += markArchived
        disposables += partialSync

        newState { it.copy(page = Inbox(messageRepo.getConversations())) }

        if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
            partialSync.execute(Unit)
        }

        markAllSeen.execute(Unit)
    }

    override fun bindView(view: MainView) {
        super.bindView(view)

        intents += view.queryChangedIntent
                .skip(1)
                .toFlowable(BackpressureStrategy.LATEST)
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { query ->
                    val filteredConversations = conversations.map { list ->
                        list.filter { conversationFilter.filter(it.conversation, query) }
                    }

                    newState { it.copy(page = Inbox(filteredConversations)) }
                }

        intents += view.composeIntent.subscribe {
            navigator.showCompose()
            newState { it.copy(drawerOpen = false) }
        }

        intents += view.drawerOpenIntent.filter { it }.subscribe {
            newState { it.copy(drawerOpen = true) }
        }

        intents += view.inboxIntent.subscribe {
            newState { it.copy(page = Inbox(conversations), drawerOpen = false) }
        }

        intents += view.archivedIntent.subscribe {
            newState { it.copy(page = Archived(messageRepo.getConversations(true)), drawerOpen = false) }
        }

        intents += view.scheduledIntent.subscribe {
            newState { it.copy(page = Scheduled(), drawerOpen = false) }
        }

        intents += view.blockedIntent.subscribe {
            newState { it.copy(page = Blocked(), drawerOpen = false) }
        }

        intents += view.settingsIntent.subscribe {
            navigator.showSettings()
            newState { it.copy(drawerOpen = false) }
        }

        intents += view.deleteConversationIntent
                .subscribe { threadId -> deleteConversation.execute(threadId) }

        intents += view.archiveConversationIntent
                .subscribe { threadId -> markArchived.execute(threadId) }
    }

}