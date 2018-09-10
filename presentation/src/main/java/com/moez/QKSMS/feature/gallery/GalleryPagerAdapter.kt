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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.mms.ContentType
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.extensions.isImage
import com.moez.QKSMS.extensions.isVideo
import com.moez.QKSMS.model.MmsPart
import com.moez.QKSMS.util.GlideApp
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.gallery_image_page.view.*
import kotlinx.android.synthetic.main.gallery_video_page.view.*
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class GalleryPagerAdapter @Inject constructor(
        context: Context,
        @Named("partId") private val partId: Long,
        private val dateFormatter: DateFormatter
) : PagerAdapter() {

    var parts: RealmResults<MmsPart>? = null
        set(value) {
            if (field === value) return
            field = value

            field?.asFlowable()
                    ?.filter { it.isLoaded }
                    ?.subscribe { notifyDataSetChanged() }
                    ?.run(disposables::add)
        }

    val clicks: Subject<View> = PublishSubject.create()

    /**
     * The Adapter isn't able to set the position itself, so it's owner must apply the initial
     * position once the data is loaded
     */
    var setInitialPositionHandler: ((Int) -> Unit)? = null

    private var loaded = false

    private val contentResolver = context.contentResolver
    private val disposables = CompositeDisposable()
    private val exoPlayers = Collections.newSetFromMap(WeakHashMap<ExoPlayer?, Boolean>())

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val part = getItem(position)
        val inflater = LayoutInflater.from(container.context)
        return when {
            part?.isImage() == true -> inflater.inflate(R.layout.gallery_image_page, container, false).apply {

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

                // We need to explicitly request a gif from glide for animations to work
                when (part.getUri().let(contentResolver::getType)) {
                    ContentType.IMAGE_GIF -> GlideApp.with(context)
                            .asGif()
                            .load(part.getUri())
                            .into(image)

                    else -> GlideApp.with(context)
                            .asBitmap()
                            .load(part.getUri())
                            .into(image)
                }

                container.addView(this)
            }

            part?.isVideo() == true -> inflater.inflate(R.layout.gallery_video_page, container, false).apply {
                val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(null)
                val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
                val exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
                video.player = exoPlayer
                exoPlayers.add(exoPlayer)

                val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "QKSMS"))
                val videoSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(part.getUri())
                exoPlayer?.prepare(videoSource)

                container.addView(this)
            }

            else -> inflater.inflate(R.layout.gallery_invalid_page, container, false).apply {
                container.addView(this)
            }
        }.apply { setOnClickListener(clicks::onNext) }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return getItem(position)?.messages?.firstOrNull()?.date?.let(dateFormatter::getDetailedTimestamp)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as? View)
    }

    override fun getCount() = parts?.size ?: 0

    fun getItem(position: Int): MmsPart? = parts?.get(position)

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()

        if (!loaded && parts?.isLoaded == true) {
            loaded = true
            parts?.indexOfFirst { it.id == partId }
                    ?.let { setInitialPositionHandler?.invoke(it) }
        }
    }

    fun destroy() {
        disposables.dispose()
        exoPlayers.forEach { exoPlayer -> exoPlayer?.release() }
    }

}