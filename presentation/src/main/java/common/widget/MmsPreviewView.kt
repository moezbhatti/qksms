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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.moez.QKSMS.R
import common.Navigator
import common.util.GlideApp
import common.util.extensions.setVisible
import injection.appComponent
import kotlinx.android.synthetic.main.mms_preview_list_item.view.*
import model.MmsPart
import util.extensions.hasThumbnails
import util.extensions.isImage
import util.extensions.isVideo
import javax.inject.Inject

class MmsPreviewView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    @Inject lateinit var navigator: Navigator

    private val layoutInflater = LayoutInflater.from(context)

    var parts: List<MmsPart> = ArrayList()
        set(value) {
            field = value
            updateView()
        }

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }

        orientation = LinearLayout.VERTICAL
    }

    private fun updateView() {
        val media = parts.filter { it.hasThumbnails() }
        setVisible(media.isNotEmpty())

        if (childCount > 0) {
            removeAllViews()
        }

        media.forEach { part ->
            val view = layoutInflater.inflate(R.layout.mms_preview_list_item, this, false)
            view.video.setVisible(part.isVideo())

            GlideApp.with(context).load(part.getUri()).fitCenter().into(view.thumbnail)

            setOnClickListener {
                when {
                    part.isImage() -> navigator.showImage(part.id)
                    part.isVideo() -> navigator.showVideo(part.getUri(), part.type)
                }
            }

            addView(view)
        }
    }

}