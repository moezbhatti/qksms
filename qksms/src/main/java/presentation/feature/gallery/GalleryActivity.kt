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
package presentation.feature.gallery

import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import common.di.appComponent
import common.util.GlideApp
import common.util.extensions.setVisible
import kotlinx.android.synthetic.main.gallery_activity.*
import presentation.common.base.QkActivity

class GalleryActivity : QkActivity<GalleryViewModel>(), GalleryView {

    override val viewModelClass = GalleryViewModel::class
    override val screenTouchedIntent by lazy { image.clicks() }

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = ""
        viewModel.bindView(this)
    }

    override fun render(state: GalleryState) {
        navigation.setVisible(state.navigationVisible)

        if (image.drawable == null) {
            GlideApp.with(this).load(state.imagePath).into(image)
        }
    }

}