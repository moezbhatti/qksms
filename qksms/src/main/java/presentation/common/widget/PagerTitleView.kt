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
import android.content.res.ColorStateList
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.moez.QKSMS.R
import com.uber.autodispose.android.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.Colors
import common.util.extensions.forEach
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.tab_view.view.*
import javax.inject.Inject

class PagerTitleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    @Inject lateinit var colors: Colors

    var pager: ViewPager? = null
        set(value) {
            if (field !== value) {
                field = value
                recreate()
            }
        }

    init {
        if (!isInEditMode) appComponent.inject(this)
    }

    private fun recreate() {
        removeAllViews()

        pager?.adapter?.count?.forEach { position ->
            val view = LayoutInflater.from(context).inflate(R.layout.tab_view, this, false)
            view.label.text = pager?.adapter?.getPageTitle(position)
            view.setOnClickListener { pager?.currentItem = position }

            addView(view)
        }

        pager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                childCount.forEach { index ->
                    getChildAt(index).isSelected = index == position
                }
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val states = arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf(-android.R.attr.state_selected))

        Observables
                .combineLatest(colors.theme, colors.textSecondary, { theme, textSecondary ->
                    ColorStateList(states, intArrayOf(theme, textSecondary))
                })
                .autoDisposable(scope())
                .subscribe { colorStateList ->
                    childCount.forEach { index ->
                        (getChildAt(index) as? TextView)?.setTextColor(colorStateList)
                    }
                }

        colors.toolbarColor
                .autoDisposable(scope())
                .subscribe { color -> setBackgroundColor(color) }
    }

}
