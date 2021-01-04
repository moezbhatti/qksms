/*
 * Copyright (C) 2020 Moez Bhatti <moez.bhatti@gmail.com>
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

import android.Manifest
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.databinding.GalleryControllerBinding
import com.moez.QKSMS.feature.gallery.injection.GalleryModule
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.model.MmsPart
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class GalleryController(val partId: Long = 0): QkController<GalleryView, GalleryState, GalleryPresenter, GalleryControllerBinding>(
        GalleryControllerBinding::inflate
), GalleryView {

    @Inject override lateinit var presenter: GalleryPresenter

    @Inject lateinit var dateFormatter: DateFormatter
    @Inject lateinit var pagerAdapter: GalleryPagerAdapter

    private val optionsItemSubject: Subject<Int> = PublishSubject.create()
    private val pageChangedSubject: Subject<MmsPart> = PublishSubject.create()

    init {
        appComponent
                .galleryBuilder()
                .galleryModule(GalleryModule(this))
                .build()
                .inject(this)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton(true)
        presenter.bindIntents(this)
    }

    override fun onViewCreated() {
        binding.pager.adapter = pagerAdapter
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                this@GalleryController.onPageSelected(position)
            }
        })

        pagerAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                pagerAdapter.data?.takeIf { pagerAdapter.itemCount > 0 }
                        ?.indexOfFirst { part -> part.id == partId }
                        ?.let { index ->
                            onPageSelected(index)
                            binding.pager.setCurrentItem(index, false)
                            pagerAdapter.unregisterAdapterDataObserver(this)
                        }
            }
        })
    }

    fun onPageSelected(position: Int) {
        binding.toolbarSubtitle.text = pagerAdapter.getItem(position)?.messages?.firstOrNull()?.date
                ?.let(dateFormatter::getDetailedTimestamp)
        binding.toolbarSubtitle.isVisible = binding.toolbarTitle.text.isNotBlank()

        pagerAdapter.getItem(position)?.run(pageChangedSubject::onNext)
    }

    override fun render(state: GalleryState) {
        binding.toolbar.setVisible(state.navigationVisible)

        setTitle(state.title)
        pagerAdapter.updateData(state.parts)
    }

    override fun optionsItemSelected(): Observable<Int> = optionsItemSubject

    override fun screenTouched(): Observable<*> = pagerAdapter.clicks

    override fun pageChanged(): Observable<MmsPart> = pageChangedSubject

    override fun requestStoragePermission() {
        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.gallery, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity!!.onBackPressed()
            else -> optionsItemSubject.onNext(item.itemId)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        pagerAdapter.destroy()
    }

}
