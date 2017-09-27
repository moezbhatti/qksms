package com.moez.QKSMS.data.repository

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import com.moez.QKSMS.data.model.Contact
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
    fun getContactBlocking(recipientId: Long): Contact {
        // First, try loading the conversation from Realm
        val realm = Realm.getDefaultInstance()
        val results = realm.where(Contact::class.java)
                .equalTo("recipientId", recipientId)
                .findAll()

        if (results.size > 0) {
            realm.close()
            return results[0]
        }

        // If it's not found, then load from the Contacts ContentProvider instead
        var contact: Contact? = null

        val recipientCursor = context.contentResolver.query(Uri.withAppendedPath(URI, recipientId.toString()), null, null, null, null)
        if (recipientCursor.moveToFirst()) {
            contact = Contact(recipientId, recipientCursor.getString(0))
        }
        recipientCursor.close()

        val contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contact?.address))
        var contactCursor: Cursor? = null
        try {
            contactCursor = context.contentResolver.query(contactUri, arrayOf(BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.Data.PHOTO_THUMBNAIL_URI), null, null, null)
            if (contactCursor.moveToFirst()) {
                contact?.name = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
                contact?.photoUri = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI))
            }
        } catch (ignored: Exception) {
        } finally {
            contactCursor?.close()
        }

        if (contact != null) {
            realm.close()
            return contact
        }

        // If it's still not found then return an empty Contact object
        return Contact()
    }

    // TODO cache the photos
    fun getAvatar(uriString: String): Maybe<Bitmap> {
        return Maybe.just(uriString)
                .subscribeOn(Schedulers.io())
                .filter { string -> string.isNotEmpty() }
                .map { string -> Uri.parse(string) }
                .map { uri -> context.contentResolver.openAssetFileDescriptor(uri, "r") }
                .map { assetFileDescriptor ->
                    val bitmap = BitmapFactory.decodeFileDescriptor(assetFileDescriptor.fileDescriptor, null, null)
                    assetFileDescriptor.close()
                    bitmap
                }
                .observeOn(AndroidSchedulers.mainThread())
    }

}