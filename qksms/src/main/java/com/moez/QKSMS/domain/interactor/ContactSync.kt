package com.moez.QKSMS.domain.interactor

import android.content.Context
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.mapper.CursorToContact
import io.reactivex.Flowable
import io.realm.Realm
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class ContactSync @Inject constructor(
        private val context: Context,
        private val cursorToContact: CursorToContact)
    : Interactor<Unit, Long>() {

    override fun buildObservable(params: Unit): Flowable<Long> {
        val contentResolver = context.contentResolver
        var realm: Realm? = null

        var startTime = 0L

        return Flowable.just(params)
                .doOnNext {
                    Timber.v("Starting contact sync")
                    startTime = System.currentTimeMillis()

                    // We need to set up realm on the io thread, and doOnSubscribe doesn't support setting a custom Scheduler
                    realm = Realm.getDefaultInstance()
                    realm?.beginTransaction()
                }
                .flatMap { contentResolver.query(CursorToContact.URI, CursorToContact.PROJECTION, null, null, null).asFlowable() }
                .map { cursor -> cursorToContact.map(cursor) }
                .groupBy { contact -> contact.lookupKey }
                .flatMap { group -> group.toList().toFlowable() }
                .map { contacts ->
                    val numbers = contacts.map { it.numbers }.flatten()
                    val contact = contacts.first()
                    contact.numbers.clear()
                    contact.numbers.addAll(numbers)
                    contact
                }
                .toList().toFlowable()
                .doOnNext { contacts ->
                    Timber.v("${contacts.size} contacts")
                    realm?.insertOrUpdate(contacts)
                }
                .doOnNext {
                    realm?.commitTransaction()
                    realm?.close()
                    Timber.v("Synced contacts in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)} seconds")
                }
                .map { 0L }
    }

}