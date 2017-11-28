package com.moez.QKSMS.presentation.messages

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
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class MessagesViewModel(val threadId: Long) : QkViewModel<MessagesView, MessagesState>(MessagesState(editingMode = threadId == 0L)) {

    @Inject lateinit var context: Context
    @Inject lateinit var contactsRepo: ContactRepository
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var sendMessage: SendMessage
    @Inject lateinit var markRead: MarkRead
    @Inject lateinit var deleteMessage: DeleteMessage

    private val contactsSubject: Subject<List<Contact>> = BehaviorSubject.create()
    private val selectedContactsReducer: Subject<(List<Contact>) -> List<Contact>> = BehaviorSubject.create()

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

        val contactsFlowable = contactsSubject.toFlowable(BackpressureStrategy.BUFFER)

        val selectedContacts: Flowable<List<Contact>> = selectedContactsReducer
                .scan(listOf<Contact>(), { previousState, reducer -> reducer(previousState) })
                .toFlowable(BackpressureStrategy.BUFFER)

        newState { it.copy(contacts = contactsFlowable, selectedContacts = selectedContacts) }

        dataChanged()
    }

    override fun bindView(view: MessagesView) {
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
                .subscribe { contacts -> contactsSubject.onNext(contacts) }

        intents += view.chipSelectedIntent.subscribe { contact ->
            selectedContactsReducer.onNext { it.toMutableList().apply { add(contact) } }
        }

        intents += view.chipDeletedIntent.subscribe { contact ->
            selectedContactsReducer.onNext { it.filterNot { it == contact } }
        }

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