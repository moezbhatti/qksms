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
package common.widget

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.moez.QKSMS.R
import common.Navigator
import common.util.Colors
import common.util.GlideApp
import common.util.extensions.setBackgroundTint
import common.util.extensions.setTint
import injection.appComponent
import kotlinx.android.synthetic.main.avatar_view.view.*
import model.Contact
import model.Recipient
import javax.inject.Inject

class AvatarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    @Inject lateinit var colors: Colors
    @Inject lateinit var navigator: Navigator

    /**
     * This value can be changes if we should use the theme from a particular conversation
     */
    var threadId: Long = 0
        set(value) {
            if (field == value) return
            field = value

            colors.theme(value).run {
                setBackgroundTint(theme)
                initial.setTextColor(textPrimary)
                icon.setTint(textPrimary)
            }
        }

    private var lookupKey: String? = null
    private var name: String? = null
    private var address: String? = null

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }

        View.inflate(context, R.layout.avatar_view, this)

        setBackgroundResource(R.drawable.circle)
        clipToOutline = true

        setOnClickListener {
            if (lookupKey.isNullOrEmpty()) {
                address?.let { address -> navigator.addContact(address) }
            } else {
                val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
                ContactsContract.QuickContact.showQuickContact(context, this@AvatarView, uri,
                        ContactsContract.QuickContact.MODE_MEDIUM, null)
            }
        }
    }

    fun setContact(recipient: Recipient?) {
        // If the recipient has a contact, just use that and return
        recipient?.contact?.let { contact ->
            setContact(contact)
            return
        }

        lookupKey = null
        name = null
        address = recipient?.address
        updateView()
    }

    fun setContact(contact: Contact?) {
        lookupKey = contact?.lookupKey
        name = contact?.name
        address = contact?.numbers?.firstOrNull()?.address
        updateView()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            colors.theme(threadId).run {
                setBackgroundTint(theme)
                initial.setTextColor(textPrimary)
                icon.setTint(textPrimary)
            }

            updateView()
        }
    }

    private fun updateView() {
        if (name?.isNotEmpty() == true) {
            initial.text = name?.substring(0, 1)
            icon.visibility = GONE
        } else {
            initial.text = null
            icon.visibility = VISIBLE
        }

        photo.setImageDrawable(null)
        address?.let { address ->
            GlideApp.with(photo).load(PhoneNumberUtils.stripSeparators(address)).into(photo)
        }
    }
}