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
package com.moez.QKSMS.common.util

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.moez.QKSMS.R
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FontProvider @Inject constructor(context: Context) {

    private var lato: Typeface? = null
    private val pendingCallbacks = ArrayList<(Typeface) -> Unit>()

    init {
        ResourcesCompat.getFont(context, R.font.lato, object : ResourcesCompat.FontCallback() {
            override fun onFontRetrievalFailed(reason: Int) {
                Timber.w("Font retrieval failed: $reason")
            }

            override fun onFontRetrieved(typeface: Typeface) {
                lato = typeface

                pendingCallbacks.forEach { lato?.run(it) }
                pendingCallbacks.clear()
            }
        }, null)
    }

    fun getLato(callback: (Typeface) -> Unit) {
        lato?.run(callback) ?: pendingCallbacks.add(callback)
    }

}