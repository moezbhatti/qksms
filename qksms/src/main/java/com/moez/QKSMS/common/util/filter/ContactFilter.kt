package com.moez.QKSMS.common.util.filter

import com.moez.QKSMS.data.model.Contact
import javax.inject.Inject

class ContactFilter @Inject constructor(private val phoneNumberFilter: PhoneNumberFilter) : Filter<Contact>() {

    override fun filter(item: Contact, query: CharSequence): Boolean {
        return item.name.contains(query, true) || // Name
                item.numbers.map { it.address }.any { address -> phoneNumberFilter.filter(address, query) } // Number
    }

}