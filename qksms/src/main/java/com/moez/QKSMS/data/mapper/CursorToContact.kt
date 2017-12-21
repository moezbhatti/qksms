package com.moez.QKSMS.data.mapper

import android.database.Cursor
import android.provider.ContactsContract
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.PhoneNumber
import javax.inject.Inject

class CursorToContact @Inject constructor() : Mapper<Cursor, Contact> {

    companion object {
        val URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

        val LOOKUP_KEY = 0
        val NUMBER = 1
        val DISPLAY_NAME = 2
    }

    override fun map(from: Cursor) = Contact().apply {
        lookupKey = from.getString(LOOKUP_KEY)
        name = from.getString(DISPLAY_NAME)
        numbers.add(PhoneNumber().apply { address = from.getString(NUMBER) })
        lastUpdate = System.currentTimeMillis()
    }

}