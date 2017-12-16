package com.moez.QKSMS.presentation.main

import android.content.Context
import android.provider.Telephony
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.filter.ConversationFilter
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.*
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.common.base.QkViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
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
    @Inject lateinit var markUnarchived: MarkUnarchived
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

        intents += view.composeIntent
                .subscribe { navigator.showCompose() }

        intents += view.drawerOpenIntent
                .subscribe { open -> newState { it.copy(drawerOpen = open) } }

        intents += view.drawerItemIntent
                .doOnNext { newState { it.copy(drawerOpen = false) } }
                .doOnNext { if (it == DrawerItem.SETTINGS) navigator.showSettings() }
                .distinctUntilChanged()
                .doOnNext {
                    when (it) {
                        DrawerItem.INBOX -> newState { it.copy(page = Inbox(conversations)) }
                        DrawerItem.ARCHIVED -> newState { it.copy(page = Archived(messageRepo.getConversations(true))) }
                        DrawerItem.SCHEDULED -> newState { it.copy(page = Scheduled()) }
                        DrawerItem.BLOCKED -> newState { it.copy(page = Blocked()) }
                    }
                }
                .subscribe()

        intents += view.deleteConversationIntent
                .subscribe { threadId -> deleteConversation.execute(threadId) }

        val archivedConversation = view.archiveConversationIntent
                .withLatestFrom(conversations.toObservable(), { position, conversations -> conversations[position] })
                .map { pair -> pair.conversation }
                .map { conversation -> conversation.id }

        intents += archivedConversation
                .withLatestFrom(state, { threadId, state ->
                    markArchived.execute(threadId) {
                        if (state.page is Inbox) {
                            val page = state.page.copy(showArchivedSnackbar = true)
                            newState { it.copy(page = page) }
                        }
                    }
                })
                .flatMap { Observable.timer(2750, TimeUnit.MILLISECONDS) }
                .withLatestFrom(state, { threadId, state ->
                    markArchived.execute(threadId) {
                        if (state.page is Inbox) {
                            val page = state.page.copy(showArchivedSnackbar = false)
                            newState { it.copy(page = page) }
                        }
                    }
                })
                .subscribe()

        intents += view.unarchiveConversationIntent
                .withLatestFrom(archivedConversation, { _, threadId -> threadId })
                .subscribe { threadId -> markUnarchived.execute(threadId) }
    }

}