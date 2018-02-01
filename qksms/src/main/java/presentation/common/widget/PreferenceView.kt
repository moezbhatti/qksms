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
package presentation.common.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutCompat
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.moez.QKSMS.R
import com.uber.autodispose.android.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.Colors
import common.util.extensions.getColorCompat
import common.util.extensions.setTint
import common.util.extensions.setVisible
import kotlinx.android.synthetic.main.preference_view.view.*
import javax.inject.Inject

class PreferenceView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayoutCompat(context, attrs) {

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

        View.inflate(context, R.layout.preference_view, this)
        setBackgroundResource(R.drawable.ripple)
        orientation = VERTICAL

        context.obtainStyledAttributes(attrs, R.styleable.PreferenceView)?.run {
            title = getString(R.styleable.PreferenceView_title)
            summary = getString(R.styleable.PreferenceView_summary)

            // If there's a custom view used for the preference's widget, inflate it
            getResourceId(R.styleable.PreferenceView_widget, -1).takeIf { it != -1 }?.let { id ->
                View.inflate(context, id, widgetFrame)
            }

            // If an icon is being used, set up the icon view
            getResourceId(R.styleable.PreferenceView_icon, -1).takeIf { it != -1 }?.let { id ->
                icon.setVisible(true)
                icon.setImageResource(id)
            }

            recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (isInEditMode) {
            icon.setTint(context.getColorCompat(R.color.textSecondary))
        } else {
            colors.textSecondary
                    .autoDisposable(scope())
                    .subscribe { icon.setTint(it) }
        }
    }

}