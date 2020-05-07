/*
 * Copyright (C) 2019 Moez Bhatti <moez.bhatti@gmail.com>
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
package com.moez.QKSMS.feature.compose.editing

import android.content.Context
import android.os.Build
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.databinding.ContactChipBinding
import com.moez.QKSMS.model.Recipient
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ChipsAdapter @Inject constructor() : QkAdapter<Recipient, ContactChipBinding>() {

    var view: RecyclerView? = null
    val chipDeleted: PublishSubject<Recipient> = PublishSubject.create<Recipient>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<ContactChipBinding> {
        return QkViewHolder(parent, ContactChipBinding::inflate).apply {
            // These theme attributes don't apply themselves on API 21
            if (Build.VERSION.SDK_INT <= 22) {
                binding.content.setBackgroundTint(parent.context.resolveThemeColor(R.attr.bubbleColor))
            }

            binding.root.setOnClickListener {
                val chip = getItem(adapterPosition)
                showDetailedChip(parent.context, chip)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder<ContactChipBinding>, position: Int) {
        val recipient = getItem(position)

        holder.binding.avatar.setRecipient(recipient)
        holder.binding.name.text = recipient.contact?.name?.takeIf { it.isNotBlank() } ?: recipient.address
    }

    /**
     * The [context] has to come from a view, because we're inflating a view that used themed attrs
     */
    private fun showDetailedChip(context: Context, recipient: Recipient) {
        val detailedChipView = DetailedChipView(context)
        detailedChipView.setRecipient(recipient)

        val rootView = view?.rootView as ViewGroup

        val layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        layoutParams.topMargin = 32.dpToPx(context)
        layoutParams.marginStart = 56.dpToPx(context)
        layoutParams.marginEnd = 8.dpToPx(context)

        rootView.addView(detailedChipView, layoutParams)
        detailedChipView.show()

        detailedChipView.setOnDeleteListener {
            chipDeleted.onNext(recipient)
            detailedChipView.hide()
        }
    }
}
