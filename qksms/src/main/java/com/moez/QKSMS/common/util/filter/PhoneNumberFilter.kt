package com.moez.QKSMS.common.util.filter

import android.telephony.PhoneNumberUtils
import javax.inject.Inject

class PhoneNumberFilter @Inject constructor() : Filter<String>() {

    override fun filter(item: String, query: CharSequence): Boolean {
        return query.all { PhoneNumberUtils.isReallyDialable(it) } &&
                PhoneNumberUtils.stripSeparators(item).contains(PhoneNumberUtils.stripSeparators(query.toString()))
    }

}