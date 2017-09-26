package com.moez.QKSMS.ui.conversations

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.data.sync.SyncManager
import com.moez.QKSMS.util.NotificationManager
import io.reactivex.subjects.PublishSubject
import io.realm.RealmResults
import javax.inject.Inject

class ConversationListViewModel : ViewModel() {

    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var messageRepo: MessageRepository

    val state: MutableLiveData<ConversationListViewState> = MutableLiveData()

    private val partialStates: PublishSubject<PartialState> = PublishSubject.create()
    private val conversations: RealmResults<Message>

    init {
        AppComponentManager.appComponent.inject(this)

        partialStates
                .scan(ConversationListViewState(), { previous, changes -> changes.reduce(previous) })
                .subscribe { newState -> state.value = newState }

        conversations = messageRepo.getConversationMessagesAsync()
        partialStates.onNext(PartialState.ConversationsLoaded(conversations))

        messageRepo.markAllSeen()
    }

    fun onRefresh() {
        partialStates.onNext(PartialState.Refreshing(true))
        syncManager.copyToRealm { partialStates.onNext(PartialState.Refreshing(false)) }
    }

}