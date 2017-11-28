package com.moez.QKSMS.presentation.compose

import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Message
import io.reactivex.Flowable
import io.realm.RealmResults

data class ComposeState(
        val editingMode: Boolean = false,
        val contacts: Flowable<List<Contact>>? = null,
        val selectedContacts: Flowable<List<Contact>>? = null,
        val title: String = "",
        val messages: RealmResults<Message>? = null,
        val draft: String = "",
        val canSend: Boolean = false
)