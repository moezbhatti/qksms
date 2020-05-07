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

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.databinding.MmsVcardListItemBinding
import com.moez.QKSMS.extensions.isVCard
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.feature.compose.BubbleUtils
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.MmsPart
import ezvcard.Ezvcard
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class VCardBinder @Inject constructor(
    colors: Colors,
    private val context: Context
) : PartBinder<MmsVcardListItemBinding>(MmsVcardListItemBinding::inflate) {

    override var theme = colors.theme()

    override fun canBindPart(part: MmsPart) = part.isVCard()

    override fun bindPartInternal(
        holder: QkViewHolder<MmsVcardListItemBinding>,
        part: MmsPart,
        message: Message,
        canGroupWithPrevious: Boolean,
        canGroupWithNext: Boolean
    ) {
        BubbleUtils.getBubble(false, canGroupWithPrevious, canGroupWithNext, message.isMe())
                .let(holder.binding.vCardBackground::setBackgroundResource)

        holder.binding.root.setOnClickListener { clicks.onNext(part.id) }

        Observable.just(part.getUri())
                .map(context.contentResolver::openInputStream)
                .mapNotNull { inputStream -> inputStream.use { Ezvcard.parse(it).first() } }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { vcard -> holder.binding.name?.text = vcard.formattedName.value }

        val params = holder.binding.vCardBackground.layoutParams as FrameLayout.LayoutParams
        if (!message.isMe()) {
            holder.binding.vCardBackground.layoutParams = params.apply { gravity = Gravity.START }
            holder.binding.vCardBackground.setBackgroundTint(theme.theme)
            holder.binding.vCardAvatar.setTint(theme.textPrimary)
            holder.binding.name.setTextColor(theme.textPrimary)
            holder.binding.label.setTextColor(theme.textTertiary)
        } else {
            holder.binding.vCardBackground.layoutParams = params.apply { gravity = Gravity.END }
            holder.binding.vCardBackground.setBackgroundTint(holder.binding.root.context.resolveThemeColor(R.attr.bubbleColor))
            holder.binding.vCardAvatar.setTint(holder.binding.root.context.resolveThemeColor(android.R.attr.textColorSecondary))
            holder.binding.name.setTextColor(holder.binding.root.context.resolveThemeColor(android.R.attr.textColorPrimary))
            holder.binding.label.setTextColor(holder.binding.root.context.resolveThemeColor(android.R.attr.textColorTertiary))
        }
    }

}