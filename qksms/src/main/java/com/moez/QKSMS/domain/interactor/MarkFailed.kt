package com.moez.QKSMS.domain.interactor

import android.net.Uri
import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkFailed @Inject constructor(val messageRepo: MessageRepository) : Interactor<Unit, MarkFailed.Params>() {

    data class Params(val uri: Uri, val resultCode: Int)

    override fun buildObservable(params: Params): Flowable<Unit> {
        return Flowable.just(Unit)
                .doOnNext { messageRepo.markFailed(params.uri, params.resultCode) }
    }

}