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
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.view.CollapsibleActionView
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.extensions.dismissKeyboard
import io.reactivex.Observable
import kotlinx.android.synthetic.main.search_view.view.*


class SearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs), CollapsibleActionView {

    val queryChanged: Observable<CharSequence>

    init {
        View.inflate(context, R.layout.search_view, this)
        orientation = LinearLayout.HORIZONTAL
        layoutParams = Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT)

        queryChanged = query.textChanges()
    }

    override fun onActionViewExpanded() {

        // Focus on the query field and display the keyboard
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        query.requestFocus()
        imm?.showSoftInput(query, 0)
    }

    override fun onActionViewCollapsed() {
        // Reset the query
        query.setText("")

        // Dismiss the keyboard
        (context as? Activity)?.dismissKeyboard()
    }

}