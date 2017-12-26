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
package com.moez.QKSMS.presentation.common.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutCompat
import android.util.AttributeSet
import android.view.View
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.extensions.setVisible
import kotlinx.android.synthetic.main.preference_view.view.*

class PreferenceView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayoutCompat(context, attrs) {

    var title: String? = null
        set(value) {
            field = value
            titleView.text = value
        }

    var summary: String? = null
        set(value) {
            field = value
            summaryView.text = value
            summaryView.setVisible(value?.isNotEmpty() == true)
        }

    init {
        View.inflate(context, R.layout.preference_view, this)
        setBackgroundResource(R.drawable.ripple)
        orientation = VERTICAL

        context.obtainStyledAttributes(attrs, R.styleable.PreferenceView)?.run {
            title = getString(R.styleable.PreferenceView_title)
            summary = getString(R.styleable.PreferenceView_summary)
            getResourceId(R.styleable.PreferenceView_widget, -1).takeIf { it != -1 }?.run {
                View.inflate(context, this, widgetFrame)
            }
            recycle()
        }
    }

}