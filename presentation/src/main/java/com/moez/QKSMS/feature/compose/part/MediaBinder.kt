package com.moez.QKSMS.feature.compose.part

import android.content.Context
import android.view.View
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.common.widget.BubbleImageView
import com.moez.QKSMS.extensions.isImage
import com.moez.QKSMS.extensions.isVideo
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.MmsPart
import com.moez.QKSMS.util.GlideApp
import kotlinx.android.synthetic.main.mms_preview_list_item.view.*

class MediaBinder(private val context: Context, private val navigator: Navigator) : PartBinder {

    override val partLayout = R.layout.mms_preview_list_item

    override fun canBindPart(part: MmsPart) = part.isImage() || part.isVideo()

    override fun bindPart(view: View, part: MmsPart, message: Message, canGroupWithPrevious: Boolean, canGroupWithNext: Boolean) {
        view.video.setVisible(part.isVideo())
        view.setOnClickListener { navigator.showMedia(part.id) }

        view.thumbnail.bubbleStyle = when {
            !canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_FIRST else BubbleImageView.Style.IN_FIRST
            canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_MIDDLE else BubbleImageView.Style.IN_MIDDLE
            canGroupWithPrevious && !canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_LAST else BubbleImageView.Style.IN_LAST
            else -> BubbleImageView.Style.ONLY
        }

        GlideApp.with(context).load(part.getUri()).fitCenter().into(view.thumbnail)
    }

}