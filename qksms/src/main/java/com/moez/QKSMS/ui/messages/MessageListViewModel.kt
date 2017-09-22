package com.moez.QKSMS.ui.messages

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class MessageListViewModel : ViewModel() {

    @Inject lateinit var messageRepo: MessageRepository

    val state: MutableLiveData<MessageListViewState> = MutableLiveData()
    var threadId: Long = 0
        set(value) {
            field = value
            newThreadId()
        }

    private val partialStates: PublishSubject<PartialState> = PublishSubject.create()
    private var conversation: Conversation? = null

    init {
        AppComponentManager.appComponent.inject(this)

        partialStates
                .scan(MessageListViewState(), { previous, changes -> changes.reduce(previous) })
                .subscribe { newState -> state.value = newState }
    }

    private fun newThreadId() {
        conversation?.removeAllChangeListeners()
        conversation = messageRepo.getConversationAsync(threadId)
        conversation?.addChangeListener { conversation: Conversation ->
            when (conversation.isValid) {
                true -> {
                    val title = conversation.getTitle()
                    val messages = conversation.messages.sort("date")
                    partialStates.onNext(PartialState.ConversationLoaded(title, messages))
                }
                false -> partialStates.onNext(PartialState.ConversationError(true))
            }
        }
    }

    fun sendMessage(body: String) {
        conversation?.takeIf { conversation -> conversation.isValid }?.let { conversation ->
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
    }

}