package com.moez.QKSMS.domain.interactor

import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkAllSeen @Inject constructor(val messageRepo: MessageRepository) : Interactor<Unit, Unit>() {

    override fun buildObservable(params: Unit): Flowable<Unit> {
        return Flowable.just(Unit)
                .doOnNext { messageRepo.markAllSeen() }
    }

}