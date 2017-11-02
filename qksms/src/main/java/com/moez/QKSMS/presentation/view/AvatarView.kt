package com.moez.QKSMS.presentation.view

import android.content.Context
import android.content.res.ColorStateList
import android.telephony.PhoneNumberUtils
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.GlideApp
import com.moez.QKSMS.common.util.extensions.getColorCompat
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
        View.inflate(context, R.layout.avatar_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
        backgroundTintList = ColorStateList.valueOf(context.getColorCompat(R.color.bubbleLight))

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        photo.setImageDrawable(null)
        initial.text = "?"

        contact?.let { contact ->
            initial.text = if (contact.name.isNotEmpty()) contact.name.substring(0, 1) else "?"
            GlideApp.with(photo).load(PhoneNumberUtils.stripSeparators(contact.address)).into(photo)
        }
    }
}