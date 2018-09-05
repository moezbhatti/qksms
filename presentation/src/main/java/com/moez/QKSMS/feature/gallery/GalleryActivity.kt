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
package com.moez.QKSMS.feature.gallery

import android.net.Uri
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.TransitionSet
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.mms.ContentType
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.GlideCompletionListener
import com.moez.QKSMS.common.base.QkActivity
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.util.GlideApp
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.gallery_activity.*
import javax.inject.Inject


class GalleryActivity : QkActivity(), GalleryView {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val screenTouchedIntent by lazy { image.clicks() }
    override val optionsItemSelectedIntent: Subject<Int> = PublishSubject.create()

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[GalleryViewModel::class.java] }

    private var exoPlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_activity)
        postponeEnterTransition()
        showBackButton(true)
        viewModel.bindView(this)

        val transition = TransitionSet().apply {
            duration = 100

            addTransition(ChangeBounds())
            addTransition(ChangeImageTransform())
        }

        window.sharedElementReturnTransition = transition
        window.sharedElementEnterTransition = transition

        // FIXME: Setting a transitionName breaks GIF playback
        // image.transitionName = intent.getLongExtra("partId", 0L).toString()

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

        when {
            ContentType.isImageType(state.type) -> showImage(state.uri)
            ContentType.isVideoType(state.type) -> showVideo(state.uri)
            else -> hideMedia()
        }
    }

    private fun showImage(uri: Uri?) {
        image.setVisible(true)
        video.setVisible(false)
        if (image.drawable == null) {

            // We need to explicitly request a gif from glide for animations to work
            when (uri?.let(contentResolver::getType)) {
                ContentType.IMAGE_GIF -> GlideApp.with(this)
                        .asGif()
                        .load(uri)
                        .listener(GlideCompletionListener {
                            startPostponedEnterTransition()
                        })
                        .into(image)

                else -> GlideApp.with(this)
                        .asBitmap()
                        .load(uri)
                        .listener(GlideCompletionListener {
                            startPostponedEnterTransition()
                        })
                        .into(image)
            }
        }
    }

    private fun showVideo(uri: Uri?) {
        image.setVisible(false)
        video.setVisible(true)

        if (video.player == null) {
            val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(null)
            val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
            video.player = exoPlayer

            val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "QKSMS"))
            val videoSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            exoPlayer?.prepare(videoSource)
        }
    }

    private fun hideMedia() {
        image.setVisible(false)
        video.setVisible(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.gallery, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> optionsItemSelectedIntent.onNext(item.itemId)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }

}