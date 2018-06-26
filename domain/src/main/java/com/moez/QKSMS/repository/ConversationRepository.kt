package com.moez.QKSMS.repository

import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.SearchResult
import io.realm.RealmResults

interface ConversationRepository {

    fun getConversations(archived: Boolean = false): RealmResults<Conversation>

    fun getConversationsSnapshot(): List<Conversation>

    fun setConversationName(id: Long, name: String)

    fun searchConversations(query: String): List<SearchResult>

    fun getBlockedConversations(): RealmResults<Conversation>

    fun getConversationAsync(threadId: Long): Conversation

    fun getConversation(threadId: Long): Conversation?

    fun getThreadId(recipient: String): Long?

    fun getThreadId(recipients: Collection<String>): Long?

    fun getOrCreateConversation(threadId: Long): Conversation?

    fun getOrCreateConversation(address: String): Conversation?

    fun getOrCreateConversation(addresses: List<String>): Conversation?

    fun saveDraft(threadId: Long, draft: String)

    /**
     * Updates message-related fields in the conversation, like the date and snippet
     */
    fun updateConversations(vararg threadIds: Long)

    fun markArchived(vararg threadIds: Long)

    fun markUnarchived(vararg threadIds: Long)

    fun markBlocked(vararg threadIds: Long)

    fun markUnblocked(vararg threadIds: Long)

    fun deleteConversations(vararg threadIds: Long)

}