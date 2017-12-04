package com.moez.QKSMS.presentation.compose

import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Message
import io.realm.RealmResults

data class ComposeState(
        val hasError: Boolean = false,
        val editingMode: Boolean = false,
        val contacts: List<Contact> = ArrayList(),
        val contactsVisible: Boolean = false,
        val selectedContacts: List<Contact> = ArrayList(),
        val title: String = "",
        val archived: Boolean = false,
        val messages: RealmResults<Message>? = null,
        val draft: String = "",
        val canSend: Boolean = false
)