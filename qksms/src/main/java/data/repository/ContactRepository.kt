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
package data.repository

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import common.util.extensions.asFlowable
import data.model.Contact
import data.model.PhoneNumber
import io.reactivex.Flowable
import io.reactivex.Single
import io.realm.Realm
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(val context: Context) {

    fun findContactUri(address: String): Single<Uri> {
        return Flowable.just(address)
                .map { address -> Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address)) }
                .flatMap { uri -> context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null).asFlowable() }
                .firstOrError()
                .map { cursor -> cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID)) }
                .map { id -> Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id) }
    }

    fun getContacts(): List<Contact> {
        val realm = Realm.getDefaultInstance()
        val results = realm.copyFromRealm(realm
                .where(Contact::class.java)
                .findAllSorted("name"))
        realm.close()

        return results
    }

    fun getContact(address: String): Flowable<Contact> {
        return Flowable.fromPublisher<Contact> { emitter ->
            getContactBlocking(address)?.let { emitter.onNext(it) }
            emitter.onComplete()
        }
    }

    fun getContactBlocking(address: String): Contact? {
        if (address.isEmpty()) return null

        val realm = Realm.getDefaultInstance()
        var contact = realm.where(Contact::class.java)
                .equalTo("numbers.address", address)
                .findFirst()

        if (contact == null) {
            val contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address))
            val projection = arrayOf(BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.LOOKUP_KEY)
            try {
                context.contentResolver.query(contactUri, projection, null, null, null).use { contactCursor ->
                    if (contactCursor.moveToFirst()) {
                        contact = Contact().apply {
                            numbers.add(PhoneNumber().apply { this.address = address })
                            name = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).orEmpty()
                            lookupKey = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY)).orEmpty()
                            lastUpdate = System.currentTimeMillis()
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.w(e)
            }
        } else {
            contact = realm.copyFromRealm(contact)
        }

        realm.close()

        return contact
    }

}