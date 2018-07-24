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
package com.moez.QKSMS.feature.compose.part

import android.content.ContentUris
import android.content.Context
import android.view.View
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.extensions.isVCard
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.feature.compose.BubbleUtils
import com.moez.QKSMS.mapper.CursorToPartImpl
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.MmsPart
import ezvcard.Ezvcard
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mms_vcard_list_item.view.*

class VCardBinder(
        private val context: Context,
        private val navigator: Navigator,
        private val theme: Colors.Theme
) : PartBinder {

    override val partLayout = R.layout.mms_vcard_list_item

    override fun canBindPart(part: MmsPart) = part.isVCard()

    override fun bindPart(view: View, part: MmsPart, message: Message, canGroupWithPrevious: Boolean, canGroupWithNext: Boolean) {
        val uri = ContentUris.withAppendedId(CursorToPartImpl.CONTENT_URI, part.id)

        view.setOnClickListener { navigator.saveVcard(uri) }
        view.vCardBackground.setBackgroundResource(BubbleUtils.getBubble(canGroupWithPrevious, canGroupWithNext, message.isMe()))

        Observable.just(uri)
                .map(context.contentResolver::openInputStream)
                .mapNotNull { inputStream -> inputStream.use { Ezvcard.parse(it).first() } }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { vcard -> view.name?.text = vcard.formattedName.value }

        if (!message.isMe()) {
            view.vCardBackground.setBackgroundTint(theme.theme)
            view.vCardAvatar.setTint(theme.textPrimary)
            view.name.setTextColor(theme.textPrimary)
            view.label.setTextColor(theme.textTertiary)
        } else {
            view.vCardBackground.setBackgroundTint(view.context.resolveThemeColor(R.attr.bubbleColor))
            view.vCardAvatar.setTint(view.context.resolveThemeColor(android.R.attr.textColorSecondary))
            view.name.setTextColor(view.context.resolveThemeColor(android.R.attr.textColorPrimary))
            view.label.setTextColor(view.context.resolveThemeColor(android.R.attr.textColorTertiary))
        }
    }

}