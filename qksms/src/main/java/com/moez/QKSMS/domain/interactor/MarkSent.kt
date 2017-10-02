package com.moez.QKSMS.domain.interactor

import android.content.ContentValues
import android.net.Uri
import android.provider.Telephony
import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkSent @Inject constructor(val messageRepo: MessageRepository) : Interactor<Unit, Uri>() {

    override fun buildObservable(params: Uri): Flowable<Unit> {
        return Flowable.just(params)
                .doOnNext {
                    val values = ContentValues()
                    values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                    messageRepo.updateMessageFromUri(values, params)
                }
                .map { Unit }
    }

}