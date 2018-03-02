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
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.TransitionSet
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import common.util.GlideApp
import common.util.extensions.setVisible
import kotlinx.android.synthetic.main.gallery_activity.*
import presentation.common.GlideCompletionListener
import presentation.common.base.QkActivity

class GalleryActivity : QkActivity<GalleryViewModel>(), GalleryView {

    override val viewModelClass = GalleryViewModel::class
    override val screenTouchedIntent by lazy { image.clicks() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_activity)
        postponeEnterTransition()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        val transition = TransitionSet().apply {
            duration = 100

            addTransition(ChangeBounds())
            addTransition(ChangeImageTransform())
        }

        window.sharedElementReturnTransition = transition
        window.sharedElementEnterTransition = transition

        image.transitionName = intent.getLongExtra("partId", 0L).toString()

        // When calling the public setter, it doesn't allow the midscale to be the same as the
        // maxscale or the minscale. We don't want 3 levels and we don't want to modify the library
        // so let's celebrate the invention of reflection!
        image.attacher.run {
            javaClass.getDeclaredField("mMinScale").run {
                isAccessible = true
                setFloat(image.attacher, 1f)
            }
            javaClass.getDeclaredField("mMidScale").run {
                isAccessible = true
                setFloat(image.attacher, 1f)
            }
            javaClass.getDeclaredField("mMaxScale").run {
                isAccessible = true
                setFloat(image.attacher, 3f)
            }
        }
    }

    override fun render(state: GalleryState) {
        toolbar.setVisible(state.navigationVisible)

        title = state.title

        if (image.drawable == null) {
            GlideApp.with(this)
                    .load(state.imagePath)
                    .dontAnimate()
                    .listener(GlideCompletionListener {
                        startPostponedEnterTransition()
                    })
                    .into(image)
        }
    }

}