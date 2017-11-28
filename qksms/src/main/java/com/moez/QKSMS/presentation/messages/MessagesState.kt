package com.moez.QKSMS.presentation.messages

import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Message
import io.reactivex.Flowable
import io.realm.RealmResults

data class MessagesState(
        val editingMode: Boolean = false,
        val contacts: Flowable<List<Contact>>? = null,
        val selectedContacts: Flowable<List<Contact>>? = null,
        val title: String = "",
        val messages: RealmResults<Message>? = null,
        val draft: String = "",
        val canSend: Boolean = false
)