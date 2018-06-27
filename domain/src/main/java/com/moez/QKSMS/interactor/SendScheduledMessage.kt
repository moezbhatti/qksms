package com.moez.QKSMS.interactor

import android.content.Context
import android.net.Uri
import com.moez.QKSMS.compat.TelephonyCompat
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.model.Attachment
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.MessageRepository
import com.moez.QKSMS.repository.ScheduledMessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class SendScheduledMessage @Inject constructor(
        private val context: Context,
        private val conversationRepo: ConversationRepository,
        private val messageRepo: MessageRepository,
        private val scheduledMessageRepo: ScheduledMessageRepository
) : Interactor<Long>() {

    override fun buildObservable(params: Long): Flowable<*> {
        return Flowable.just(params)
                .mapNotNull(scheduledMessageRepo::getScheduledMessage)
                .map { message ->
                    TelephonyCompat.getOrCreateThreadId(context, message.recipients).also { threadId ->
                        if ((message.recipients.size == 1 || !message.sendAsGroup) && message.attachments.isEmpty()) {
                            message.recipients.forEach { address ->
                                messageRepo.sendSmsAndPersist(message.subId, threadId, address, message.body)
                            }
                        } else {
                            val attachments = message.attachments.mapNotNull(Uri::parse).map { Attachment(it) }
                            messageRepo.sendMms(message.subId, threadId, message.recipients, message.body, attachments)
                        }
                    }
                }
                .doOnNext { scheduledMessageRepo.deleteScheduledMessage(params) }
                // If this was the first message sent in the conversation, the conversation might not exist yet
                .doOnNext { conversationRepo.getOrCreateConversation(it) }
                .doOnNext { conversationRepo.updateConversations(it) }
                .doOnNext { conversationRepo.markUnarchived(it) }
    }

}