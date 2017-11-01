package com.moez.QKSMS.data.repository

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import com.moez.QKSMS.data.model.Contact
import io.reactivex.Flowable
import io.realm.Realm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(val context: Context) {

    /**
     * Uri for the MMS-SMS recipients table, where there exists only one column (address)
     */
    private val URI = Uri.parse("content://mms-sms/canonical-address")

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
                val projection = arrayOf(BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.Data.PHOTO_URI)
                context.contentResolver.query(contactUri, projection, null, null, null).use { contactCursor ->
                    if (contactCursor.moveToFirst()) {
                        contact.name = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)).orEmpty()
                        contact.photoUri = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Data.PHOTO_URI)).orEmpty()
                    }
                }
                return contact
            }
        }

        return null
    }

    fun getContactFromCache(address: String): Contact? {
        val realm = Realm.getDefaultInstance()
        val contact = realm.where(Contact::class.java)
                .equalTo("address", address)
                .findFirst()
        realm.close()
        return contact
    }

}