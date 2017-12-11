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