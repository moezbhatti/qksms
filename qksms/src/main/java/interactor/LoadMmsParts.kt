package interactor

import data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class LoadMmsParts @Inject constructor(private val messageRepo: MessageRepository): Interactor<Long, Long>() {

    override fun buildObservable(params: Long): Flowable<Long> {
        return Flowable.just(params)
                .doOnNext { messageId -> messageRepo.loadParts(messageId) }
    }

}