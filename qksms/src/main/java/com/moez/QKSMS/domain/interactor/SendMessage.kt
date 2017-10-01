package com.moez.QKSMS.domain.interactor

import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class SendMessage @Inject constructor(val messageRepo: MessageRepository) : Interactor<Unit, SendMessage.Params>() {

    data class Params(val threadId: Long, val address: String, val body: String)

    override fun buildUseCaseObservable(params: Params): Flowable<Unit> {
        return Flowable.just(Unit)
                .doOnNext { messageRepo.sendMessage(params.threadId, params.address, params.body) }
    }

}