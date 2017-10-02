package com.moez.QKSMS.domain.interactor

import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import com.moez.QKSMS.common.util.NotificationManager
import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class ReceiveMessage @Inject constructor(
        val context: Context,
        val messageRepo: MessageRepository,
        val notificationManager: NotificationManager) : Interactor<Unit, ReceiveMessage.Params>() {

    data class Params(val address: String, val body: String, val sentTime: Long)

    override fun buildObservable(params: Params): Flowable<Unit> {
        val values = ContentValues()
        values.put(Telephony.Sms.ADDRESS, params.address)
        values.put(Telephony.Sms.BODY, params.body)
        values.put(Telephony.Sms.DATE_SENT, params.sentTime)
        values.put(Telephony.Sms.READ, false)
        values.put(Telephony.Sms.SEEN, false)

        val contentResolver = context.contentResolver
        return Flowable.just(values)
                .map { contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values) }
                .doOnNext { uri -> messageRepo.addMessageFromUri(uri) }
                .doOnNext { notificationManager.update(messageRepo) }
                .map { Unit }
    }

}