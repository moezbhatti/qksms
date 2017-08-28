package com.moez.QKSMS.ui.messages

import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.MessageRepository
import io.realm.RealmResults

class MessageListViewModel(threadId: Long) : ViewModel() {

    private val repository = MessageRepository(threadId)

    val messages: RealmResults<Message> = repository.getMessages()

}