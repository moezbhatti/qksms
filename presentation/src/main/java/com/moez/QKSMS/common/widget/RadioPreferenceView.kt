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

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.forwardTouches
import com.moez.QKSMS.common.util.extensions.resolveThemeAttribute
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.injection.appComponent
import kotlinx.android.synthetic.main.radio_preference_view.view.*
import javax.inject.Inject

class RadioPreferenceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    @Inject lateinit var colors: Colors

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
        if (!isInEditMode) {
            appComponent.inject(this)
        }

        View.inflate(context, R.layout.radio_preference_view, this)
        setBackgroundResource(context.resolveThemeAttribute(R.attr.selectableItemBackground))

        val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked))

        val themeColor = when (isInEditMode) {
            true -> context.resources.getColor(R.color.tools_theme)
            false -> colors.theme().theme
        }
        val textSecondary = context.resolveThemeColor(android.R.attr.textColorTertiary)
        radioButton.buttonTintList = ColorStateList(states, intArrayOf(themeColor, textSecondary))
        radioButton.forwardTouches(this)

        context.obtainStyledAttributes(attrs, R.styleable.RadioPreferenceView)?.run {
            title = getString(R.styleable.RadioPreferenceView_title)
            summary = getString(R.styleable.RadioPreferenceView_summary)

            // If there's a custom view used for the preference's widget, inflate it
            getResourceId(R.styleable.RadioPreferenceView_widget, -1).takeIf { it != -1 }?.let { id ->
                View.inflate(context, id, widgetFrame)
            }

            recycle()
        }
    }

}