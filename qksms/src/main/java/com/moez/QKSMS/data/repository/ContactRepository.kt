package com.moez.QKSMS.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
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

        val cursor = context.contentResolver.query(ContentUris.withAppendedId(URI, recipientId), null, null, null, null)
        if (cursor.moveToFirst()) {
            contact = Contact(recipientId, cursor.getString(0))
        }
        cursor.close()

        return contact
    }

}