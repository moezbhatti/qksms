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
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.model.Recipient
import kotlinx.android.synthetic.main.group_avatar_view.view.*

class GroupAvatarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    var contacts: List<Recipient> = ArrayList()
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

            // If we're on API 21 we need to reapply the tint after changing the background
            if (Build.VERSION.SDK_INT < 22) {
                avatar.applyTheme(0)
            }
        }

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        avatars.forEachIndexed { index, avatar ->
            avatar.visibility = if (contacts.size > index) View.VISIBLE else View.GONE
            avatar.setContact(contacts.getOrNull(index))
        }
    }

}