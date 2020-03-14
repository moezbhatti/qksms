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
package com.moez.QKSMS.feature.compose

import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.databinding.AttachmentContactListItemBinding
import com.moez.QKSMS.databinding.AttachmentImageListItemBinding
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.model.Attachment
import ezvcard.Ezvcard
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class AttachmentAdapter @Inject constructor(
    private val context: Context
) : QkAdapter<Attachment, ViewBinding>() {

    companion object {
        private const val VIEW_TYPE_IMAGE = 0
        private const val VIEW_TYPE_CONTACT = 1
    }

    val attachmentDeleted: Subject<Attachment> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<ViewBinding> {
        val holder: QkViewHolder<ViewBinding> = when (viewType) {
            VIEW_TYPE_IMAGE -> QkViewHolder(parent, AttachmentImageListItemBinding::inflate)
            VIEW_TYPE_CONTACT -> QkViewHolder(parent, AttachmentContactListItemBinding::inflate)
            else -> null!! // Impossible
        }

        return holder.apply {
            if (binding is AttachmentImageListItemBinding) {
                binding.thumbnailBounds.clipToOutline = true
            }

            binding.root.setOnClickListener {
                val attachment = getItem(adapterPosition)
                attachmentDeleted.onNext(attachment)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder<ViewBinding>, position: Int) {
        val attachment = getItem(position)

        when {
            attachment is Attachment.Image && holder.binding is AttachmentImageListItemBinding -> {
                Glide.with(context)
                        .load(attachment.getUri())
                        .into(holder.binding.thumbnail)
            }

            attachment is Attachment.Contact && holder.binding is AttachmentContactListItemBinding -> {
                Observable.just(attachment.vCard)
                        .mapNotNull { vCard -> Ezvcard.parse(vCard).first() }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { vcard -> holder.binding.name.text = vcard.formattedName.value }
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is Attachment.Image -> VIEW_TYPE_IMAGE
        is Attachment.Contact -> VIEW_TYPE_CONTACT
    }

}