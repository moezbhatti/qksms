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
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkAdapter
import kotlinx.android.synthetic.main.qk_dialog.view.*

class QkDialog(private val context: Activity) : AlertDialog(context) {

    private val view = LayoutInflater.from(context).inflate(R.layout.qk_dialog, null)

    @StringRes
    var titleRes: Int? = null
        set(value) {
            field = value
            title = value?.let(context::getString)
        }

    var title: String? = null
        set(value) {
            field = value
            view.title.text = value
            view.title.isVisible = !value.isNullOrBlank()
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
            view.subtitle.text = value
            view.subtitle.isVisible = !value.isNullOrBlank()
        }

    var adapter: QkAdapter<*>? = null
        set(value) {
            field = value
            view.list.isVisible = value != null
            view.list.adapter = value
        }

    var positiveButtonListener: (() -> Unit)? = null

    @StringRes
    var positiveButton: Int? = null
        set(value) {
            field = value
            value?.run(view.positiveButton::setText)
            view.positiveButton.isVisible = value != null
            view.positiveButton.setOnClickListener {
                positiveButtonListener?.invoke() ?: dismiss()
            }
        }

    var negativeButtonListener: (() -> Unit)? = null

    @StringRes
    var negativeButton: Int? = null
        set(value) {
            field = value
            value?.run(view.negativeButton::setText)
            view.negativeButton.isVisible = value != null
            view.negativeButton.setOnClickListener {
                negativeButtonListener?.invoke() ?: dismiss()
            }
        }

    var cancelListener: (() -> Unit)? = null
        set(value) {
            field = value
            setOnCancelListener { value?.invoke() }
        }

    init {
        setView(view)
    }

}
