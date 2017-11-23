package com.moez.QKSMS.domain.interactor

import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class DeleteMessage @Inject constructor(private val messageRepository: MessageRepository): Interactor<Long, Long>() {

    override fun buildObservable(params: Long): Flowable<Long> {
        return Flowable.just(params)
                .doOnNext { messageId -> messageRepository.deleteMessage(messageId) }
    }

}