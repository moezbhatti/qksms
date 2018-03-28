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
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.uber.autodispose.android.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.Navigator
import common.util.Colors
import common.util.GlideApp
import common.util.extensions.setTint
import injection.appComponent
import kotlinx.android.synthetic.main.avatar_view.view.*
import model.Contact
import javax.inject.Inject

class AvatarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    @Inject lateinit var colors: Colors
    @Inject lateinit var navigator: Navigator

    /**
     * This value can be changes if we should use the theme from a particular conversation
     */
    var threadId: Long = 0

    var contact: Contact? = null
        set(value) {
            field = value
            updateView()
        }

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }

        View.inflate(context, R.layout.avatar_view, this)

        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            colors.themeForConversation(threadId)
                    .autoDisposable(scope())
                    .subscribe { color -> background.setTint(color) }

            colors.textPrimaryOnTheme
                    .autoDisposable(scope())
                    .subscribe { color -> icon.setTint(color) }

            clicks()
                    .autoDisposable(scope())
                    .subscribe {
                        if (contact?.lookupKey.isNullOrEmpty()) {
                            contact?.numbers?.firstOrNull()?.let { number ->
                                navigator.addContact(number.address)
                            }
                        } else {
                            val key = contact?.lookupKey
                            val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, key)
                            ContactsContract.QuickContact.showQuickContact(context, this@AvatarView, uri,
                                    ContactsContract.QuickContact.MODE_MEDIUM, null)
                        }
                    }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        if (contact?.name.orEmpty().isNotEmpty()) {
            initial.text = contact?.name?.substring(0, 1)
            icon.visibility = GONE
        } else {
            initial.text = null
            icon.visibility = VISIBLE
        }

        photo.setImageDrawable(null)
        contact?.numbers?.firstOrNull()?.address?.let { address ->
            GlideApp.with(photo).load(PhoneNumberUtils.stripSeparators(address)).into(photo)
        }
    }
}