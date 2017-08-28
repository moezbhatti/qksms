package com.moez.QKSMS.ui.messages

import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.MessageRepository
import io.realm.RealmResults

class MessageListViewModel(val threadId: Long, repository: MessageRepository) : ViewModel() {

    val messages: RealmResults<Message> = repository.getMessages()

}