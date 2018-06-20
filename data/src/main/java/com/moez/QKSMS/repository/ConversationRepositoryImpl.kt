package com.moez.QKSMS.repository

import android.content.ContentUris
import android.content.Context
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import com.moez.QKSMS.compat.TelephonyCompat
import com.moez.QKSMS.extensions.anyOf
import com.moez.QKSMS.extensions.map
import com.moez.QKSMS.filter.ConversationFilter
import com.moez.QKSMS.mapper.CursorToConversation
import com.moez.QKSMS.mapper.CursorToRecipient
import com.moez.QKSMS.model.Contact
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.SearchResult
import com.moez.QKSMS.util.tryOrNull
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
        private val context: Context,
        private val conversationFilter: ConversationFilter,
        private val cursorToConversation: CursorToConversation,
        private val cursorToRecipient: CursorToRecipient
) : ConversationRepository {

    override fun getConversations(archived: Boolean): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .notEqualTo("id", 0L)
                .greaterThan("count", 0)
                .equalTo("archived", archived)
                .equalTo("blocked", false)
                .isNotEmpty("recipients")
                .sort("date", Sort.DESCENDING)
                .findAllAsync()
    }

    override fun getConversationsSnapshot(): List<Conversation> {
        val realm = Realm.getDefaultInstance()
        return realm.copyFromRealm(realm.where(Conversation::class.java)
                .notEqualTo("id", 0L)
                .greaterThan("count", 0)
                .equalTo("archived", false)
                .equalTo("blocked", false)
                .isNotEmpty("recipients")
                .sort("date", Sort.DESCENDING)
                .findAll())
    }

    override fun setConversationName(id: Long, name: String) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                realm.where(Conversation::class.java)
                        .equalTo("id", id)
                        .findFirst()
                        ?.name = name
            }
        }
    }

    override fun searchConversations(query: String): List<SearchResult> {
        val conversations = getConversationsSnapshot()

        val messagesByConversation = Realm.getDefaultInstance()
                .where(Message::class.java)
                .contains("body", query, Case.INSENSITIVE)
                .findAll()
                .groupBy { message -> message.threadId }
                .filter { (threadId, _) -> conversations.firstOrNull { it.id == threadId } != null }
                .map { (threadId, messages) -> Pair(conversations.first { it.id == threadId }, messages.size) }
                .map { (conversation, messages) -> SearchResult(query, conversation, messages) }
                .sortedByDescending { result -> result.messages }

        return conversations
                .filter { conversation -> conversationFilter.filter(conversation, query) }
                .map { conversation -> SearchResult(query, conversation, 0) }
                .plus(messagesByConversation)
    }

    override fun getBlockedConversations(): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("blocked", true)
                .findAllAsync()
    }

    override fun getConversationAsync(threadId: Long): Conversation {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirstAsync()
    }

    override fun getConversation(threadId: Long): Conversation? {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()
    }

    override fun getThreadId(recipient: String): Long? {
        return getThreadId(listOf(recipient))
    }

    override fun getThreadId(recipients: Collection<String>): Long? {
        return Realm.getDefaultInstance().use { realm ->
            realm.where(Conversation::class.java)
                    .findAll()
                    .firstOrNull { conversation ->
                        conversation.recipients.size == recipients.size && conversation.recipients.map { it.address }.all { address ->
                            recipients.any { recipient -> PhoneNumberUtils.compare(recipient, address) }
                        }
                    }
                    ?.id
        }
    }

    override fun getOrCreateConversation(threadId: Long): Conversation? {
        return getConversation(threadId) ?: getConversationFromCp(threadId)
    }

    override fun getOrCreateConversation(address: String): Conversation? {
        return getOrCreateConversation(listOf(address))
    }

    override fun getOrCreateConversation(addresses: List<String>): Conversation? {
        return tryOrNull { TelephonyCompat.getOrCreateThreadId(context, addresses.toSet()) }
                ?.takeIf { threadId -> threadId != 0L }
                ?.let { threadId ->
                    var conversation = getConversation(threadId)
                    if (conversation != null) conversation = Realm.getDefaultInstance().copyFromRealm(conversation)

                    conversation ?: getConversationFromCp(threadId)
                }
    }

    override fun saveDraft(threadId: Long, draft: String) {
        Realm.getDefaultInstance().use { realm ->
            val conversation = realm.where(Conversation::class.java)
                    .equalTo("id", threadId)
                    .findFirst()

            conversation?.let {
                realm.executeTransaction {
                    conversation.draft = draft
                }
            }
        }
    }

    override fun updateConversations(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            threadIds.forEach { threadId ->
                val conversation = realm
                        .where(Conversation::class.java)
                        .equalTo("id", threadId)
                        .findFirst() ?: return

                val messages = realm
                        .where(Message::class.java)
                        .equalTo("threadId", threadId)
                        .sort("date", Sort.DESCENDING)
                        .findAll()

                val message = messages.firstOrNull()

                realm.executeTransaction {
                    conversation.count = messages.size
                    conversation.date = message?.date ?: 0
                    conversation.snippet = message?.getSummary() ?: ""
                    conversation.read = message?.read ?: true
                    conversation.me = message?.isMe() ?: false
                }
            }
        }
    }

    override fun markArchived(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                    .anyOf("id", threadIds)
                    .findAll()

            realm.executeTransaction {
                conversations.forEach { it.archived = true }
            }
        }
    }

    override fun markUnarchived(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                    .anyOf("id", threadIds)
                    .findAll()

            realm.executeTransaction {
                conversations.forEach { it.archived = false }
            }
        }
    }

    override fun markBlocked(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                    .anyOf("id", threadIds)
                    .findAll()

            realm.executeTransaction {
                conversations.forEach { it.blocked = true }
            }
        }
    }

    override fun markUnblocked(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                    .anyOf("id", threadIds)
                    .equalTo("archived", false)
                    .findAll()

            realm.executeTransaction {
                conversations.forEach { it.blocked = false }
            }
        }
    }

    override fun deleteConversations(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversation = realm.where(Conversation::class.java).anyOf("id", threadIds).findAll()
            val messages = realm.where(Message::class.java).anyOf("threadId", threadIds).findAll()

            realm.executeTransaction {
                conversation.deleteAllFromRealm()
                messages.deleteAllFromRealm()
            }
        }

        threadIds.forEach { threadId ->
            val uri = ContentUris.withAppendedId(Telephony.Threads.CONTENT_URI, threadId)
            context.contentResolver.delete(uri, null, null)
        }
    }

    /**
     * Returns a [Conversation] from the system SMS ContentProvider, based on the [threadId]
     *
     * It should be noted that even if we have a valid [threadId], that does not guarantee that
     * we can return a [Conversation]. On some devices, the ContentProvider won't return the
     * conversation unless it contains at least 1 message
     */
    private fun getConversationFromCp(threadId: Long): Conversation? {
        return cursorToConversation.getConversationCursor(threadId)
                ?.takeIf { cursor -> cursor.moveToFirst() }
                ?.use { cursor ->
                    val conversation = cursorToConversation.map(cursor)

                    val realm = Realm.getDefaultInstance()
                    val contacts = realm.copyFromRealm(realm.where(Contact::class.java).findAll())

                    val recipients = conversation.recipients
                            .map { recipient -> recipient.id }
                            .map { id -> cursorToRecipient.getRecipientCursor(id) }
                            .mapNotNull { recipientCursor ->
                                // Map the recipient cursor to a list of recipients
                                recipientCursor?.use { recipientCursor.map { cursorToRecipient.map(recipientCursor) } }
                            }
                            .flatten()
                            .map { recipient ->
                                recipient.apply {
                                    contact = contacts.firstOrNull {
                                        it.numbers.any { PhoneNumberUtils.compare(recipient.address, it.address) }
                                    }
                                }
                            }

                    conversation.recipients.clear()
                    conversation.recipients.addAll(recipients)
                    realm.executeTransaction { it.insertOrUpdate(conversation) }
                    realm.close()

                    conversation
                }
    }

}