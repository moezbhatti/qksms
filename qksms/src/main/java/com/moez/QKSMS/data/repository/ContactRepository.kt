package com.moez.QKSMS.data.repository

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import com.moez.QKSMS.common.util.extensions.asFlowable
import com.moez.QKSMS.data.model.Contact
import io.reactivex.Flowable
import io.reactivex.Single
import io.realm.Realm
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(val context: Context) {

    /**
     * Uri for the MMS-SMS recipients table, where there exists only one column (address)
     */
    private val URI = Uri.parse("content://mms-sms/canonical-address")

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
                .equalTo("inContactsTable", true)
                .findAllSorted("name"))
        realm.close()

        return results
    }

    fun getContact(recipientId: Long): Flowable<Contact> {
        return Flowable.fromPublisher<Contact> { emitter ->
            val contact = getContactFromRealm(recipientId) ?: getContactFromDb(recipientId)
            contact?.let { emitter.onNext(it) }
            emitter.onComplete()
        }
    }

    private fun getContactFromRealm(recipientId: Long): Contact? {
        val realm = Realm.getDefaultInstance()
        val results = realm.where(Contact::class.java)
                .equalTo("recipientId", recipientId)
                .findAll()

        realm.close()
        return results.getOrNull(0)
    }

    private fun getContactFromDb(recipientId: Long): Contact? {
        context.contentResolver.query(Uri.withAppendedPath(URI, recipientId.toString()), null, null, null, null)?.use { recipientCursor ->
            if (recipientCursor.moveToFirst()) {
                val contact = Contact(recipientId, recipientCursor.getString(0))

                val contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contact.address))
                val projection = arrayOf(BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME)
                try {
                    context.contentResolver.query(contactUri, projection, null, null, null).use { contactCursor ->
                        if (contactCursor.moveToFirst()) {
                            contact.inContactsTable = true
                            contact.name = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).orEmpty()
                        }
                    }
                } catch (e: Exception) {
                    Timber.w(e)
                }
                return contact
            }
        }

        return null
    }

}