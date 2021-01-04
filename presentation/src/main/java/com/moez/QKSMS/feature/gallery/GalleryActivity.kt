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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.moez.QKSMS.common.base.QkActivity
import com.moez.QKSMS.common.util.extensions.viewBinding
import com.moez.QKSMS.databinding.ContainerActivityBinding
import com.moez.QKSMS.feature.contacts.ContactsActivity
import com.moez.QKSMS.feature.contacts.ContactsController
import dagger.android.AndroidInjection

class GalleryActivity : QkActivity() {

    private val binding by viewBinding(ContainerActivityBinding::inflate)
    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        toolbar.isVisible = false

        router = Conductor.attachRouter(this, binding.container, savedInstanceState)
        if (!router.hasRootController()) {
            val partId = intent.extras?.getLong("partId") ?: 0
            router.setRoot(RouterTransaction.with(GalleryController(partId)))
        }
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

}
