package com.moez.QKSMS.interactor

import android.content.Context
import android.net.Uri
import com.moez.QKSMS.compat.TelephonyCompat
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.model.Attachment
import com.moez.QKSMS.repository.ScheduledMessageRepository
import io.reactivex.Flowable
import io.reactivex.rxkotlin.toFlowable
import io.realm.RealmList
import javax.inject.Inject

class SendScheduledMessage @Inject constructor(
        private val context: Context,
        private val scheduledMessageRepo: ScheduledMessageRepository,
        private val sendMessage: SendMessage
) : Interactor<Long>() {

    override fun buildObservable(params: Long): Flowable<*> {
        return Flowable.just(params)
                .mapNotNull(scheduledMessageRepo::getScheduledMessage)
                .flatMap { message ->
                    if (message.sendAsGroup) {
                        listOf(message)
                    } else {
                        message.recipients.map { recipient -> message.copy(recipients = RealmList(recipient)) }
                    }.toFlowable()
                }
                .map { message ->
                    val threadId = TelephonyCompat.getOrCreateThreadId(context, message.recipients)
                    val attachments = message.attachments.mapNotNull(Uri::parse).map { Attachment(it) }
                    SendMessage.Params(message.subId, threadId, message.recipients, message.body, attachments)
                }
                .flatMap(sendMessage::buildObservable)
                .doFinally { scheduledMessageRepo.deleteScheduledMessage(params) }
    }

}