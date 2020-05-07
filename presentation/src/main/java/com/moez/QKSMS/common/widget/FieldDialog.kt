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
package com.moez.QKSMS.common.widget

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.moez.QKSMS.R
import com.moez.QKSMS.databinding.FieldDialogBinding

class FieldDialog(context: Activity, hint: String, listener: (String) -> Unit) : AlertDialog(context) {

    private val binding = FieldDialogBinding.inflate(context.layoutInflater)

    init {
        binding.field.hint = hint

        setView(binding.root)
        setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.button_cancel)) { _, _ -> }
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.button_delete)) { _, _ -> listener("") }
        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.button_save)) { _, _ ->
            listener(binding.field.text.toString())
        }
    }

    fun setText(text: String): FieldDialog {
        binding.field.setText(text)
        return this
    }

}
