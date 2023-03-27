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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.extensions.getDisplayName
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.model.Attachment
import com.moez.QKSMS.util.GlideApp
import ezvcard.Ezvcard
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.attachment_contact_list_item.*
import kotlinx.android.synthetic.main.attachment_file_list_item.*
import kotlinx.android.synthetic.main.attachment_image_list_item.*
import kotlinx.android.synthetic.main.attachment_image_list_item.view.*
import javax.inject.Inject

class AttachmentAdapter @Inject constructor(
    private val context: Context
) : QkAdapter<Attachment>() {

    companion object {
        private const val VIEW_TYPE_IMAGE = 0
        private const val VIEW_TYPE_CONTACT = 1
        private const val VIEW_TYPE_VIDEO = 2
        private const val VIEW_TYPE_FILE = 3

    }

    val attachmentDeleted: Subject<Attachment> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            VIEW_TYPE_IMAGE -> inflater.inflate(R.layout.attachment_image_list_item, parent, false)
                    .apply { thumbnailBounds.clipToOutline = true }

            VIEW_TYPE_CONTACT -> inflater.inflate(R.layout.attachment_contact_list_item, parent, false)

            VIEW_TYPE_VIDEO -> inflater.inflate(R.layout.attachment_image_list_item, parent, false)
                    .apply { thumbnailBounds.clipToOutline = true }

            VIEW_TYPE_FILE -> inflater.inflate(R.layout.attachment_file_list_item, parent, false)

            else -> null!! // Impossible
        }

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val attachment = getItem(adapterPosition)
                attachmentDeleted.onNext(attachment)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val attachment = getItem(position)

        when (attachment) {
            is Attachment.Image -> Glide.with(context)
                    .load(attachment.getUri())
                    .into(holder.thumbnail)

            is Attachment.Contact -> Observable.just(attachment.vCard)
                    .mapNotNull { vCard -> Ezvcard.parse(vCard).first() }
                    .map { vcard -> vcard.getDisplayName() ?: "" }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { displayName ->
                        holder.name?.text = displayName
                        holder.name?.isVisible = displayName.isNotEmpty()
                    }
            is Attachment.Video -> {
                GlideApp.with(context).load(attachment.getUri()).fitCenter().into(holder.thumbnail)
            }
            is Attachment.File -> {
                Observable.just(attachment.getName(context))
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { name ->
                            holder.filename?.text = name
                            holder.filename?.isVisible = name?.isNotEmpty() ?: false
                        }
                Observable.just(attachment.getSize(context))
                        .map { bytes ->
                            when (bytes) {
                                in 0..999 -> "$bytes B"
                                in 1000..999999 -> "${"%.1f".format(bytes / 1000f)} KB"
                                in 1000000..9999999 -> "${"%.1f".format(bytes / 1000000f)} MB"
                                else -> "${"%.1f".format(bytes / 1000000000f)} GB"
                            }
                        }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { size ->
                            holder.size?.text = size
                            holder.size?.isVisible = size?.isNotEmpty() ?: false
                        }
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is Attachment.Image -> VIEW_TYPE_IMAGE
        is Attachment.Contact -> VIEW_TYPE_CONTACT
        is Attachment.Video -> VIEW_TYPE_VIDEO
        is Attachment.File -> VIEW_TYPE_FILE
    }

}