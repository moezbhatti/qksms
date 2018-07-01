package com.moez.QKSMS.feature.scheduled

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.GlideApp
import kotlinx.android.synthetic.main.attachment_list_item.view.*
import javax.inject.Inject

class ScheduledMessageAttachmentAdapter @Inject constructor() : QkAdapter<Uri>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scheduled_message_image_list_item, parent, false)
        view.thumbnail.clipToOutline = true

        return QkViewHolder(view)
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val attachment = getItem(position)
        val view = holder.itemView

        GlideApp.with(view).load(attachment).into(view.thumbnail)
    }

}