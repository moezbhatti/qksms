package com.moez.QKSMS.domain.interactor

import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkRead @Inject constructor(val messageRepo: MessageRepository): Interactor<Unit, Long>() {

    override fun buildUseCaseObservable(params: Long): Flowable<Unit> {
        return Flowable.just(Unit)
                .doOnNext { messageRepo.markRead(params) }
    }

}