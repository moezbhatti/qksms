package com.moez.QKSMS.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import com.moez.QKSMS.data.model.Contact

class ContactRepository(val context: Context) {

    /**
     * Uri for the MMS-SMS recipients table, where there exists only one column (address)
     */
    private val URI = Uri.parse("content://mms-sms/canonical-address")

    fun getContactBlocking(recipientId: Long): Contact {
        val cursor = context.contentResolver.query(ContentUris.withAppendedId(URI, recipientId), null, null, null, null)
        if (cursor.moveToFirst()) {
            return Contact(recipientId, cursor.getString(0))
        }
        cursor.close()

        return Contact()
    }

}