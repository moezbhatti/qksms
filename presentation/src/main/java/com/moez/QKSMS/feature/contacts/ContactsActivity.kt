/*
 * Copyright (C) 2019 Moez Bhatti <moez.bhatti@gmail.com>
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
package com.moez.QKSMS.feature.contacts

import android.os.Bundle
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.util.extensions.viewBinding
import com.moez.QKSMS.databinding.ContainerActivityBinding
import dagger.android.AndroidInjection

class ContactsActivity : QkThemedActivity() {

    companion object {
        const val SharingKey = "sharing"
        const val ChipsKey = "chips"
    }

    private val binding by viewBinding(ContainerActivityBinding::inflate)
    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        toolbar.isVisible = false

        router = Conductor.attachRouter(this, binding.container, savedInstanceState)
        if (!router.hasRootController()) {
            val isSharing = intent.extras?.getBoolean(SharingKey, false) ?: false
            val chips = intent.extras?.getSerializable(ChipsKey)
                    ?.let { serializable -> serializable as? HashMap<String, String?> }
                    ?: hashMapOf()
            router.setRoot(RouterTransaction.with(ContactsController(isSharing, chips)))
        }
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

}
