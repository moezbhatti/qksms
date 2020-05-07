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
import android.widget.FrameLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.common.util.extensions.viewBinding
import com.moez.QKSMS.databinding.AvatarViewBinding
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.model.Recipient
import com.moez.QKSMS.util.GlideApp
import javax.inject.Inject

class AvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    @Inject lateinit var colors: Colors
    @Inject lateinit var navigator: Navigator
    private lateinit var theme: Colors.Theme

    private val binding = viewBinding(AvatarViewBinding::inflate)

    private var lookupKey: String? = null
    private var name: String? = null
    private var photoUri: String? = null
    private var lastUpdated: Long? = null

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
            theme = colors.theme()
        }

        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    /**
     * Use the [contact] information to display the avatar.
     */
    fun setRecipient(recipient: Recipient?) {
        lookupKey = recipient?.contact?.lookupKey
        name = recipient?.contact?.name
        photoUri = recipient?.contact?.photoUri
        lastUpdated = recipient?.contact?.lastUpdate
        theme = colors.theme(recipient)
        updateView()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        // Apply theme
        setBackgroundTint(theme.theme)
        binding.initial.setTextColor(theme.textPrimary)
        binding.icon.setTint(theme.textPrimary)

        if (name?.isNotEmpty() == true) {
            val initials = name
                    ?.substringBefore(',')
                    ?.split(" ").orEmpty()
                    .filter { subname -> subname.isNotEmpty() }
                    .map { subname -> subname[0].toString() }

            binding.initial.text = if (initials.size > 1) initials.first() + initials.last() else initials.first()
            binding.icon.visibility = GONE
        } else {
            binding.initial.text = null
            binding.icon.visibility = VISIBLE
        }

        binding.photo.setImageDrawable(null)
        photoUri?.let { photoUri ->
            GlideApp.with(binding.photo)
                    .load(photoUri)
                    .into(binding.photo)
        }
    }
}
