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
import android.widget.ImageView
import android.widget.LinearLayout
import common.Navigator
import common.util.GlideApp
import common.util.extensions.setVisible
import injection.appComponent
import model.MmsPart
import javax.inject.Inject

class MmsPreviewView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    @Inject lateinit var navigator: Navigator

    var parts: List<MmsPart> = ArrayList()
        set(value) {
            field = value
            updateView()
        }

    init {
        appComponent.inject(this)
        orientation = LinearLayout.VERTICAL
    }

    private fun updateView() {
        val images = parts.filter { it.image != null }
        setVisible(images.isNotEmpty())

        if (childCount > 0) {
            removeAllViews()
        }

        images.forEach { image ->
            addView(ImageView(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                GlideApp.with(context).load(image.image).fitCenter().into(this)
                setOnClickListener { navigator.showImage(image.id) }
            })
        }
    }

}