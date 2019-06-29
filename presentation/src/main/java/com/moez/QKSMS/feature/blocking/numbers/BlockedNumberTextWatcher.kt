/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.feature.blocking.numbers

import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import timber.log.Timber
import java.util.*

class BlockedNumberTextWatcher(private val editText: EditText) : TextWatcher {

    init {
        editText.addTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val formatted = PhoneNumberUtils.formatNumber(s?.toString(), Locale.getDefault().country)
        Timber.v("onTextChanged=$s, formatted=$formatted")
        if (s?.toString() != formatted && formatted != null) {
            editText.setText(formatted)
            editText.setSelection(formatted.length)
        }
    }

    fun dispose() {
        editText.removeTextChangedListener(this)
    }

}
