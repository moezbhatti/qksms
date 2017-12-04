package com.moez.QKSMS.presentation.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.moez.QKSMS.R
import com.moez.QKSMS.data.model.Contact
import io.realm.RealmList
import kotlinx.android.synthetic.main.group_avatar_view.view.*

class GroupAvatarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    var contacts: RealmList<Contact> = RealmList()
        set(value) {
            field = value
            updateView()
        }

    private val avatars by lazy { listOf(avatar1, avatar2, avatar3) }

    init {
        View.inflate(context, R.layout.group_avatar_view, this)
        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        avatars.forEach { avatar ->
            avatar.setBackgroundResource(R.drawable.rectangle)
        }

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        avatars.forEachIndexed { index, avatar ->
            avatar.visibility = if (contacts.size > index) View.VISIBLE else View.GONE
            avatar.contact = if (contacts.size > index) contacts[index] else null
        }
    }

}