package com.moez.QKSMS.domain.interactor

import android.content.Context
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.common.util.extensions.insertOrUpdate
import com.moez.QKSMS.data.mapper.CursorToContact
import com.moez.QKSMS.data.model.Contact
import io.reactivex.Flowable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class ContactSync @Inject constructor(
        private val context: Context,
        private val cursorToContact: CursorToContact)
    : Interactor<Unit, List<Contact>>() {

    override fun buildObservable(params: Unit): Flowable<List<Contact>> {
        val contentResolver = context.contentResolver

        Timber.v("Starting contact sync")
        val startTime = System.currentTimeMillis()

        return contentResolver.query(CursorToContact.URI, CursorToContact.PROJECTION, null, null, null).asFlowable()
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
                    contacts.insertOrUpdate()

                    val duration = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
                    Timber.v("Synced contacts in $duration seconds")
                }
    }

}