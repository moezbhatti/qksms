package com.moez.QKSMS.presentation.messages

import android.content.Context
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.ClipboardUtils
import com.moez.QKSMS.common.util.extensions.makeToast
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.DeleteMessage
import com.moez.QKSMS.domain.interactor.MarkRead
import com.moez.QKSMS.domain.interactor.SendMessage
import com.moez.QKSMS.presentation.base.QkViewModel
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class MessagesViewModel(val threadId: Long) : QkViewModel<MessagesView, MessagesState>(MessagesState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var sendMessage: SendMessage
    @Inject lateinit var markRead: MarkRead
    @Inject lateinit var deleteMessage: DeleteMessage

    private var conversation: Conversation? = null

    init {
        AppComponentManager.appComponent.inject(this)

        disposables += sendMessage
        disposables += markRead
        disposables += messageRepo.getConversationAsync(threadId)
                .asFlowable<Conversation>()
                .filter { it.isLoaded }
                .subscribe { conversation ->
                    when (conversation.isValid) {
                        true -> {
                            this.conversation = conversation
                            val title = conversation.getTitle()
                            val messages = messageRepo.getMessages(threadId)
                            newState { it.copy(title = title, messages = messages) }
                        }
                        false -> newState { it.copy(hasError = true) }
                    }
                }

        dataChanged()
    }

    override fun bindIntents(view: MessagesView) {
        super.bindIntents(view)

        intents += view.textChangedIntent.subscribe { text ->
            newState { it.copy(draft = text.toString(), canSend = text.isNotEmpty()) }
        }

        intents += view.sendIntent.subscribe {
            val previousState = state.value!!
            val address = conversation?.contacts?.first()?.address.orEmpty()
            sendMessage.execute(SendMessage.Params(threadId, address, previousState.draft))
            newState { it.copy(draft = "", canSend = false) }
        }

        intents += view.copyTextIntent.subscribe { message ->
            ClipboardUtils.copy(context, message.body)
            context.makeToast(R.string.toast_copied)
        }

        intents += view.deleteMessageIntent.subscribe { message ->
            deleteMessage.execute(message.id)
        }
    }

    fun dataChanged() {
        markRead.execute(threadId)
    }

}