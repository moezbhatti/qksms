package com.moez.QKSMS.common.util.filter

import com.moez.QKSMS.data.model.Conversation
import javax.inject.Inject

class ConversationFilter @Inject constructor(private val recipientFilter: RecipientFilter) : Filter<Conversation>() {

    override fun filter(item: Conversation, query: String): Boolean {
        return item.recipients.any { recipient -> recipientFilter.filter(recipient, query) }
    }

}