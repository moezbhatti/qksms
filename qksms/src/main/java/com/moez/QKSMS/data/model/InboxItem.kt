package com.moez.QKSMS.data.model

/**
 * Groups a conversation with its latest message into a single model
 */
data class InboxItem(val conversation: Conversation, val message: Message)