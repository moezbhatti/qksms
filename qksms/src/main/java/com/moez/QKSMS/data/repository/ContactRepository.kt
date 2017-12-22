package com.moez.QKSMS.data.repository

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.PhoneNumber
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

        realm.close()

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
        }

        return contact
    }

}