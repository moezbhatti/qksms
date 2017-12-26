package com.moez.QKSMS.domain.interactor

import com.moez.QKSMS.common.util.Permissions
import com.moez.QKSMS.common.util.SyncManager
import io.reactivex.Flowable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class PartialSync @Inject constructor(
        private val syncManager: SyncManager,
        private val permissions: Permissions
) : Interactor<Unit, Long>() {

    override fun buildObservable(params: Unit): Flowable<Long> {
        return Flowable.just(System.currentTimeMillis())
                .skipWhile { !permissions.hasSmsAndContacts() }
                .doOnNext { syncManager.performSync() }
                .map { startTime -> System.currentTimeMillis() - startTime }
                .map { elapsed -> TimeUnit.MILLISECONDS.toSeconds(elapsed) }
                .doOnNext { seconds -> Timber.v("Completed sync in $seconds seconds") }
    }

}