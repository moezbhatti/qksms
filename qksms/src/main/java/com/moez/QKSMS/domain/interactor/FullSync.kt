package com.moez.QKSMS.domain.interactor

import com.moez.QKSMS.common.util.SyncManager
import io.reactivex.Flowable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FullSync @Inject constructor(private val syncManager: SyncManager) : Interactor<Unit, Long>() {

    override fun buildObservable(params: Unit): Flowable<Long> {
        return Flowable.just(System.currentTimeMillis())
                .doOnNext { syncManager.performSync(true) }
                .map { startTime -> System.currentTimeMillis() - startTime }
                .map { elapsed -> TimeUnit.MILLISECONDS.toSeconds(elapsed) }
                .doOnNext { seconds -> Timber.v("Completed sync in $seconds seconds") }
    }

}