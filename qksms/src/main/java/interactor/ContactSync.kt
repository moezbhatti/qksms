/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package interactor

import android.content.Context
import common.util.extensions.asFlowable
import common.util.extensions.insertOrUpdate
import data.mapper.CursorToContact
import data.model.Contact
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