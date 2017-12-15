package com.moez.QKSMS.data.mapper

import android.database.Cursor
import android.net.Uri
import com.moez.QKSMS.data.model.Recipient
import com.moez.QKSMS.data.repository.ContactRepository
import javax.inject.Inject

class CursorToRecipient @Inject constructor(private val contactRepository: ContactRepository) : Mapper<Cursor, Recipient> {

    companion object {
        val URI = Uri.parse("content://mms-sms/canonical-addresses")

        val COLUMN_ID = 0
        val COLUMN_ADDRESS = 1
    }

    override fun map(from: Cursor) = Recipient().apply {
        id = from.getLong(COLUMN_ID)
        address = from.getString(COLUMN_ADDRESS)
        contact = contactRepository.getContactBlocking(address) // TODO do this lazily for faster syncs
        lastUpdate = System.currentTimeMillis()
    }

}