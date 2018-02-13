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
package presentation.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import common.di.appComponent
import common.util.GlideApp
import common.util.extensions.mapNotNull
import common.util.extensions.setVisible
import data.model.MmsPart
import kotlinx.android.synthetic.main.mms_preview_view.view.*
import presentation.common.Navigator
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
        View.inflate(context, R.layout.mms_preview_view, this)

        image.clicks()
                .mapNotNull { parts.firstOrNull { it.image != null } }
                .map { part -> part.id }
                .doOnNext { partId -> navigator.showImage(partId) }
                .subscribe()
    }

    private fun updateView() {
        val images = parts.mapNotNull { it.image }
        setVisible(images.isNotEmpty())

        images.firstOrNull()?.let {
            GlideApp.with(context).load(it).fitCenter().into(image)
        }
    }

}