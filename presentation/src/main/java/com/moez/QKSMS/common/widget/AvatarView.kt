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
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.bumptech.glide.signature.ObjectKey
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.model.Contact
import com.moez.QKSMS.model.Recipient
import com.moez.QKSMS.util.GlideApp
import kotlinx.android.synthetic.main.avatar_view.view.*
import javax.inject.Inject

class AvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    @Inject lateinit var colors: Colors
    @Inject lateinit var navigator: Navigator

    /**
     * This value can be changes if we should use the theme from a particular conversation
     */
    var threadId: Long = 0
        set(value) {
            if (field == value) return
            field = value
            applyTheme(value)
        }

    private var lookupKey: String? = null
    private var name: String? = null
    private var address: String? = null
    private var lastUpdated: Long? = null

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }

        View.inflate(context, R.layout.avatar_view, this)

        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    /**
     * If the [recipient] has a contact: use the contact's avatar, but keep the address.
     * Use the recipient address otherwise.
     */
    fun setContact(recipient: Recipient?) {
        // If the recipient has a contact, just use that and return
        recipient?.contact?.let { contact ->
            setContact(contact, recipient.address)
            return
        }

        lookupKey = null
        name = null
        address = recipient?.address
        lastUpdated = 0
        updateView()
    }

    /**
     * Use the [contact] information to display the avatar.
     * A specific [contactAddress] can be specified (useful when the contact has several addresses).
     */
    fun setContact(contact: Contact?, contactAddress: String? = null) {
        lookupKey = contact?.lookupKey
        name = contact?.name
        // If a contactAddress has been given, we use it. Use the contact address otherwise.
        address = contactAddress ?: contact?.numbers?.firstOrNull()?.address
        lastUpdated = contact?.lastUpdate
        updateView()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            applyTheme(threadId)
            updateView()
        }
    }

    fun applyTheme(threadId: Long) {
        colors.theme(threadId).run {
            setBackgroundTint(theme)
            initial.setTextColor(textPrimary)
            icon.setTint(textPrimary)
        }
    }

    private fun updateView() {
        if (name?.isNotEmpty() == true) {
            val initials = name?.split(" ").orEmpty()
                    .filter { name -> name.isNotEmpty() }
                    .map { name -> name[0].toString() }

            initial.text = if (initials.size > 1) initials.first() + initials.last() else initials.first()
            icon.visibility = GONE
        } else {
            initial.text = null
            icon.visibility = VISIBLE
        }

        photo.setImageDrawable(null)
        address?.let { address ->
            GlideApp.with(photo)
                    .load("tel:$address")
                    .signature(ObjectKey(lastUpdated ?: 0L))
                    .into(photo)
        }
    }
}