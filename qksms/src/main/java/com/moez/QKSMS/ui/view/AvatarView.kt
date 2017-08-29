package com.moez.QKSMS.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.data.model.Contact
import kotlinx.android.synthetic.main.avatar_view.view.*

class AvatarView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : FrameLayout(context, attrs, defStyleAttr) {

    var contact: Contact? = null
        set(value) {
            field = value
            updateView()
        }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    init {
        if (!isInEditMode) {
            View.inflate(context, R.layout.avatar_view, this)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        updateView()
    }

    private fun updateView() {
        initial.text = if (contact?.name?.length ?: 0 > 0) contact?.name?.substring(0, 1) else "?"
    }

}