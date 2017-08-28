package com.moez.QKSMS.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import com.moez.QKSMS.data.model.Contact
import io.realm.Realm


class ContactRepository(val context: Context) {

    /**
     * Uri for the MMS-SMS recipients table, where there exists only one column (address)
     */
    private val URI = Uri.parse("content://mms-sms/canonical-address")

    /**
     * This function is not asynchronous, and potentially very slow. Make sure to
     * execute off of the main thread. At the time of writing
     */
    fun getContact(recipientId: Long): Contact {
        var contact: Contact? = getContactFromRealm(recipientId)
        if (contact == null) {
            contact = getContactFromContentProvider(recipientId)
        }

        return contact ?: Contact()
    }

    private fun getContactFromRealm(recipientId: Long): Contact? {
        val realm = Realm.getDefaultInstance()
        val results = realm.where(Contact::class.java)
                .equalTo("recipientId", recipientId)
                .findAll()

        if (results.size > 0) {
            return results[0]
        }

        return null
    }

    private fun getContactFromContentProvider(recipientId: Long): Contact? {
        var contact: Contact? = null

        val recipientCursor = context.contentResolver.query(ContentUris.withAppendedId(URI, recipientId), null, null, null, null)
        if (recipientCursor.moveToFirst()) {
            contact = Contact(recipientId, recipientCursor.getString(0))
        }
        recipientCursor.close()

        val contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contact?.address))
        var contactCursor: Cursor? = null
        try {
            contactCursor = context.contentResolver.query(contactUri, arrayOf(BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
            if (contactCursor.moveToFirst()) {
                contact?.name = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
            }
        } catch (ignored: Exception) {
        } finally {
            contactCursor?.close()
        }

        return contact
    }

}