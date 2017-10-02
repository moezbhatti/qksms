package com.moez.QKSMS.domain.interactor

import android.content.ContentValues
import android.net.Uri
import android.provider.Telephony
import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkFailed @Inject constructor(val messageRepo: MessageRepository) : Interactor<Unit, MarkFailed.Params>() {

    data class Params(val uri: Uri, val resultCode: Int)

    override fun buildObservable(params: Params): Flowable<Unit> {
        return Flowable.just(Unit)
                .doOnNext {
                    val values = ContentValues()
                    values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_FAILED)
                    values.put(Telephony.Sms.ERROR_CODE, params.resultCode)
                    messageRepo.updateMessageFromUri(values, params.uri)
                }
    }

}