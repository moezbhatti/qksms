package com.moez.QKSMS.presentation.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.moez.QKSMS.R
import com.moez.QKSMS.data.model.Contact
import io.realm.RealmList
import kotlinx.android.synthetic.main.group_avatar_view.view.*

class GroupAvatarView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {

    var contacts: RealmList<Contact> = RealmList()
        set(value) {
            field = value
            updateView()
        }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    init {
        View.inflate(context, R.layout.group_avatar_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        avatar1.visibility = if (contacts.size > 0) View.VISIBLE else View.GONE
        avatar1.contact = if (contacts.size > 0) contacts[0] else null
        avatar2.visibility = if (contacts.size > 1) View.VISIBLE else View.GONE
        avatar2.contact = if (contacts.size > 1) contacts[1] else null
        avatar3.visibility = if (contacts.size > 2) View.VISIBLE else View.GONE
        avatar3.contact = if (contacts.size > 2) contacts[2] else null
    }

}