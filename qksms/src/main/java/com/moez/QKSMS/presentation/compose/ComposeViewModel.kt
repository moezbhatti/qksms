package com.moez.QKSMS.presentation.compose

import android.content.Context
import android.telephony.PhoneNumberUtils
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.ClipboardUtils
import com.moez.QKSMS.common.util.extensions.makeToast
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.repository.ContactRepository
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.DeleteMessage
import com.moez.QKSMS.domain.interactor.MarkRead
import com.moez.QKSMS.domain.interactor.SendMessage
import com.moez.QKSMS.presentation.base.QkViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ComposeViewModel(threadId: Long) : QkViewModel<ComposeView, ComposeState>(ComposeState(editingMode = threadId == 0L)) {

    @Inject lateinit var context: Context
    @Inject lateinit var contactsRepo: ContactRepository
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var sendMessage: SendMessage
    @Inject lateinit var markRead: MarkRead
    @Inject lateinit var deleteMessage: DeleteMessage

    private val contacts: List<Contact>

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
                    }
                }

        contacts = contactsRepo.getContacts()

        dataChanged()
    }

    override fun bindView(view: ComposeView) {
        super.bindView(view)

        intents += view.queryChangedIntent
                .toFlowable(BackpressureStrategy.LATEST)
                .map { query -> query.toString() }
                .map { query ->
                    contacts.filter { contact ->
                        contact.name.contains(query, true) || PhoneNumberUtils.compare(contact.address, query)
                    }
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contacts -> newState { it.copy(contacts = contacts) } }

        intents += Observable.merge<(List<Contact>) -> List<Contact>>(
                view.chipDeletedIntent.map { contact ->
                    { contacts: List<Contact> -> contacts.filterNot { it == contact } }
                },
                view.chipSelectedIntent.map { contact ->
                    { contacts: List<Contact> -> contacts.toMutableList().apply { add(contact) } }
                })
                .scan(state.value!!.contacts, { previousState, reducer -> reducer(previousState) })
                .doOnNext { contacts -> newState { it.copy(selectedContacts = contacts) } }
                .subscribe()

        intents += view.textChangedIntent.subscribe { text ->
            newState { it.copy(draft = text.toString(), canSend = text.isNotEmpty()) }
        }

        intents += view.sendIntent
                .withLatestFrom(view.textChangedIntent, { _, body -> body })
                .map { body -> body.toString() }
                .subscribe { body ->
                    val threadId = conversation?.id ?: 0
                    val address = conversation?.contacts?.first()?.address.orEmpty()
                    sendMessage.execute(SendMessage.Params(threadId, address, body))
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
        conversation?.id?.let { threadId ->
            markRead.execute(threadId)
        }
    }

}