package com.moez.QKSMS.domain.interactor

import android.net.Uri
import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkSent @Inject constructor(val messageRepo: MessageRepository) : Interactor<Unit, Uri>() {

    override fun buildObservable(params: Uri): Flowable<Unit> {
        return Flowable.just(Unit)
                .doOnNext { messageRepo.markSent(params) }
    }

}