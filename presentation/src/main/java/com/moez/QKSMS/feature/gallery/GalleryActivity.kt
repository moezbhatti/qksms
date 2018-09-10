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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkActivity
import com.moez.QKSMS.common.util.extensions.addOnPageChangeListener
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.model.MmsPart
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.gallery_activity.*
import javax.inject.Inject

class GalleryActivity : QkActivity(), GalleryView {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var pagerAdapter: GalleryPagerAdapter

    private val optionsItemSubject: Subject<Int> = PublishSubject.create()
    private val pageChangedSubject: Subject<MmsPart> = PublishSubject.create()
    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[GalleryViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_activity)
        showBackButton(true)
        viewModel.bindView(this)

        pagerAdapter.setInitialPositionHandler = { position ->
            pager.setCurrentItem(position, false)

            // The ViewPager's page change listener isn't called if the initial position is 0
            if (position == 0) {
                pageChanged(0)
            }
        }
        pager.adapter = pagerAdapter
        pager.addOnPageChangeListener(this::pageChanged)
    }

    private fun pageChanged(position: Int) {
        toolbarSubtitle.text = pagerAdapter.getPageTitle(position)
        toolbarSubtitle.isVisible = toolbarTitle.text.isNotBlank()

        pagerAdapter.getItem(position)?.run(pageChangedSubject::onNext)
    }

    override fun render(state: GalleryState) {
        toolbar.setVisible(state.navigationVisible)

        title = state.title
        pagerAdapter.parts = state.parts
    }

    override fun optionsItemSelected(): Observable<Int> = optionsItemSubject

    override fun screenTouched(): Observable<*> = pagerAdapter.clicks

    override fun pageChanged(): Observable<MmsPart> = pageChangedSubject

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.gallery, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> optionsItemSubject.onNext(item.itemId)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        pagerAdapter.destroy()
    }

}