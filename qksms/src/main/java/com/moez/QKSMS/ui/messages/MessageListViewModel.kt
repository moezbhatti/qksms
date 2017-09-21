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

    val state: MutableLiveData<MessageListViewState> = MutableLiveData()
    var threadId: Long = 0
        set(value) {
            field = value
            newThreadId()
        }

    private val partialStates: PublishSubject<PartialState> = PublishSubject.create()
    private var conversation: RealmResults<Conversation>? = null
    private var messages: RealmResults<Message>? = null

    init {
        AppComponentManager.appComponent.inject(this)

        partialStates
                .scan(MessageListViewState(), { previous, changes -> changes.reduce(previous) })
                .subscribe { newState -> state.value = newState }
    }

    private fun newThreadId() {
        messages = messageRepo.getMessages(threadId)
        messages?.let { partialStates.onNext(PartialState.MessagesLoaded(it)) }

        conversation = conversationRepo.getConversation(threadId)
        conversation?.addChangeListener { realmResults ->
            when (realmResults.size) {
                0 -> partialStates.onNext(PartialState.ConversationError(true))
                else -> partialStates.onNext(PartialState.ConversationLoaded(realmResults[0]))
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