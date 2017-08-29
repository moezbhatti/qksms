package com.moez.QKSMS.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class AvatarView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ImageView(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

}