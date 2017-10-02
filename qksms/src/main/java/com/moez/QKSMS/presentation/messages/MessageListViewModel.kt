package com.moez.QKSMS.presentation.messages

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.MarkRead
import com.moez.QKSMS.domain.interactor.SendMessage
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class MessageListViewModel : ViewModel() {

    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var sendMessage: SendMessage
    @Inject lateinit var markRead: MarkRead

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
        dataChanged()
        conversation?.removeAllChangeListeners()
        conversation = messageRepo.getConversationAsync(threadId)
        conversation?.addChangeListener { conversation: Conversation ->
            when (conversation.isValid) {
                true -> {
                    val title = conversation.getTitle()
                    val messages = messageRepo.getMessages(threadId)
                    partialStates.onNext(PartialState.ConversationLoaded(title, messages))
                }
                false -> partialStates.onNext(PartialState.ConversationError(true))
            }
        }
    }

    fun sendMessage(body: String) {
        conversation?.takeIf { conversation -> conversation.isValid }?.let { conversation ->
            sendMessage.execute({}, SendMessage.Params(threadId, conversation.contacts[0].address, body))
            partialStates.onNext(PartialState.TextChanged(""))
        }
    }

    fun dataChanged() {
        markRead.execute({}, threadId)
    }

    fun textChanged(text: String) {
        partialStates.onNext(PartialState.TextChanged(text))
    }

    override fun onCleared() {
        super.onCleared()
        sendMessage.dispose()
        markRead.dispose()
        conversation?.removeAllChangeListeners()
    }

}