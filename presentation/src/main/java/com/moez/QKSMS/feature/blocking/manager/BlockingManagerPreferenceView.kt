/*
 * Copyright (C) 2020 Moez Bhatti <moez.bhatti@gmail.com>
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

package com.moez.QKSMS.feature.blocking.manager

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.extensions.animateLayoutChanges
import com.moez.QKSMS.common.util.extensions.resolveThemeAttribute
import com.moez.QKSMS.common.util.extensions.setVisible
import kotlinx.android.synthetic.main.blocking_manager_preference_view.view.*

class BlockingManagerPreferenceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    var icon: Drawable? = null
        set(value) {
            field = value

            if (isInEditMode) {
                findViewById<ImageView>(R.id.iconView).setImageDrawable(value)
            } else {
                iconView.setImageDrawable(value)
            }
        }

    var title: String? = null
        set(value) {
            field = value

            if (isInEditMode) {
                findViewById<TextView>(R.id.titleView).text = value
            } else {
                titleView.text = value
            }
        }

    var summary: String? = null
        set(value) {
            field = value

            if (isInEditMode) {
                findViewById<TextView>(R.id.summaryView).run {
                    text = value
                    setVisible(value?.isNotEmpty() == true)
                }
            } else {
                summaryView.text = value
                summaryView.setVisible(value?.isNotEmpty() == true)
            }
        }

    init {
        View.inflate(context, R.layout.blocking_manager_preference_view, this)
        setBackgroundResource(context.resolveThemeAttribute(R.attr.selectableItemBackground))

        context.obtainStyledAttributes(attrs, R.styleable.BlockingManagerPreferenceView).run {
            icon = getDrawable(R.styleable.BlockingManagerPreferenceView_icon)
            title = getString(R.styleable.BlockingManagerPreferenceView_title)
            summary = getString(R.styleable.BlockingManagerPreferenceView_summary)

            // If there's a custom view used for the preference's widget, inflate it
            getResourceId(R.styleable.BlockingManagerPreferenceView_widget, -1).takeIf { it != -1 }?.let { id ->
                View.inflate(context, id, widgetFrame)
            }

            recycle()
        }
    }
}
