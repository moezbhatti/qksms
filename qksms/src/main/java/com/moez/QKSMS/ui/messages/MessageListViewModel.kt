package com.moez.QKSMS.ui.messages

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.ConversationRepository
import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.subjects.PublishSubject
import io.realm.RealmResults
import javax.inject.Inject

class MessageListViewModel : ViewModel() {

    @Inject lateinit var conversationRepo: ConversationRepository
    @Inject lateinit var messageRepo: MessageRepository

    val partialStates: PublishSubject<PartialState> = PublishSubject.create()
    val state: MutableLiveData<MessageListViewState> = MutableLiveData()

    private var threadId: Long = 0

    private var conversation: RealmResults<Conversation>? = null
    private var messages: RealmResults<Message>? = null

    init {
        AppComponentManager.appComponent.inject(this)

        val initialState = MessageListViewState()
        partialStates
                .scan(initialState, { previousState, changes -> changes.reduce(previousState) })
                .subscribe { newState -> state.value = newState }
    }

    fun setThreadId(threadId: Long) {
        onCleared()

        this.threadId = threadId

        messageRepo.getMessages(threadId).let {
            messages = it
            partialStates.onNext(PartialState.MessagesLoaded(it))
        }

        conversationRepo.getConversation(threadId).let {
            conversation = it
            it.addChangeListener { realmResults ->
                when (realmResults.size) {
                    0 -> partialStates.onNext(PartialState.ConversationError(true))
                    else -> partialStates.onNext(PartialState.ConversationLoaded(realmResults[0]))
                }
            }
        }
    }

    fun sendMessage(body: String) {
        conversation?.get(0)?.let { conversation ->
            messageRepo.sendMessage(threadId, conversation.contacts[0].address, body)
            partialStates.onNext(PartialState.TextChanged(""))
        }
    }

    fun textChanged(text: String) {
        partialStates.onNext(PartialState.TextChanged(text))
    }

    override fun onCleared() {
        super.onCleared()
        conversation?.removeAllChangeListeners()
        messages?.removeAllChangeListeners()
    }

}