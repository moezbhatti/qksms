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
import com.moez.QKSMS.R
import com.uber.autodispose.android.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.Colors
import common.util.extensions.getColorCompat
import javax.inject.Inject

class Separator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    @Inject lateinit var colors: Colors

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (isInEditMode) {
            setBackgroundColor(context.getColorCompat(R.color.separatorLight))
        } else {
            colors.separator
                    .autoDisposable(scope())
                    .subscribe { color -> setBackgroundColor(color) }
        }
    }
}