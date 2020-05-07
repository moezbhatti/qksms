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

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.moez.QKSMS.util.PhoneNumberUtils

class BlockedNumberTextWatcher(
    private val editText: EditText,
    private val phoneNumberUtils: PhoneNumberUtils
) : TextWatcher {

    init {
        editText.addTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable?) {
        editText.removeTextChangedListener(this)

        val formatted = s?.let(phoneNumberUtils::formatNumber)
        if (s?.toString() != formatted && formatted != null) {
            editText.setText(formatted)
            editText.setSelection(formatted.length)
        }

        editText.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    fun dispose() {
        editText.removeTextChangedListener(this)
    }

}
