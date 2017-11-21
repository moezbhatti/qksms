package com.moez.QKSMS.presentation.compose

import com.moez.QKSMS.data.model.Contact
import io.reactivex.Flowable

data class ComposeState(
        val contacts: Flowable<List<Contact>>? = null,
        val selectedContacts: Flowable<List<Contact>>? = null
)