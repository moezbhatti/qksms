/*
 * Copyright (C) 2019 Moez Bhatti <moez.bhatti@gmail.com>
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
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.databinding.QkDialogBinding

class QkDialog(private val context: Activity) : AlertDialog(context) {

    private val binding = QkDialogBinding.inflate(context.layoutInflater)

    @StringRes
    var titleRes: Int? = null
        set(value) {
            field = value
            title = value?.let(context::getString)
        }

    var title: String? = null
        set(value) {
            field = value
            binding.title.text = value
            binding.title.isVisible = !value.isNullOrBlank()
        }

    @StringRes
    var subtitleRes: Int? = null
        set(value) {
            field = value
            subtitle = value?.let(context::getString)
        }

    var subtitle: String? = null
        set(value) {
            field = value
            binding.subtitle.text = value
            binding.subtitle.isVisible = !value.isNullOrBlank()
        }

    var adapter: QkAdapter<*, *>? = null
        set(value) {
            field = value
            binding.list.isVisible = value != null
            binding.list.adapter = value
        }

    var positiveButtonListener: (() -> Unit)? = null

    @StringRes
    var positiveButton: Int? = null
        set(value) {
            field = value
            value?.run(binding.positiveButton::setText)
            binding.positiveButton.isVisible = value != null
            binding.positiveButton.setOnClickListener {
                positiveButtonListener?.invoke() ?: dismiss()
            }
        }

    var negativeButtonListener: (() -> Unit)? = null

    @StringRes
    var negativeButton: Int? = null
        set(value) {
            field = value
            value?.run(binding.negativeButton::setText)
            binding.negativeButton.isVisible = value != null
            binding.negativeButton.setOnClickListener {
                negativeButtonListener?.invoke() ?: dismiss()
            }
        }

    var cancelListener: (() -> Unit)? = null
        set(value) {
            field = value
            setOnCancelListener { value?.invoke() }
        }

    init {
        setView(binding.root)
    }

}
