package com.moez.QKSMS.ui.conversations

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.ConversationRepository
import com.moez.QKSMS.data.sync.SyncManager
import com.moez.QKSMS.util.NotificationHelper
import io.reactivex.subjects.PublishSubject
import io.realm.RealmResults
import javax.inject.Inject

class ConversationListViewModel : ViewModel() {

    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var notificationManager: NotificationHelper
    @Inject lateinit var conversationRepo: ConversationRepository

    val state: MutableLiveData<ConversationListViewState> = MutableLiveData()

    private val partialStates: PublishSubject<PartialState> = PublishSubject.create()
    private val conversations: RealmResults<Conversation>

    init {
        AppComponentManager.appComponent.inject(this)

        notificationManager.update()

        partialStates
                .scan(ConversationListViewState(), { previous, changes -> changes.reduce(previous) })
                .subscribe { newState -> state.value = newState }

        conversations = conversationRepo.getConversations()
        partialStates.onNext(PartialState.ConversationsLoaded(conversations))
    }

    fun onRefresh() {
        partialStates.onNext(PartialState.Refreshing(true))
        syncManager.copyToRealm { partialStates.onNext(PartialState.Refreshing(false)) }
    }

}