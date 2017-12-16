package com.moez.QKSMS.domain.interactor

import android.net.Uri
import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkSent @Inject constructor(private val messageRepo: MessageRepository) : Interactor<Uri, Unit>() {

    override fun buildObservable(params: Uri): Flowable<Unit> {
        return Flowable.just(Unit)
                .doOnNext { messageRepo.markSent(params) }
    }

}