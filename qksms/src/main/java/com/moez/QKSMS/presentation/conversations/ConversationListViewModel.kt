package com.moez.QKSMS.presentation.conversations

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.data.sync.SyncManager
import com.moez.QKSMS.domain.interactor.MarkAllSeen
import io.reactivex.subjects.PublishSubject
import io.realm.RealmResults
import javax.inject.Inject

class ConversationListViewModel : ViewModel() {

    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var markAllSeen: MarkAllSeen

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

        markAllSeen.execute({}, Unit)
    }

    fun onRefresh() {
        partialStates.onNext(PartialState.Refreshing(true))
        syncManager.copyToRealm { partialStates.onNext(PartialState.Refreshing(false)) }
    }

    override fun onCleared() {
        super.onCleared()
        markAllSeen.dispose()
    }

}