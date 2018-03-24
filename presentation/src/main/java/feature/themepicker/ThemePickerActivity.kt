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
package feature.themepicker

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MotionEvent
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.base.QkThemedActivity
import common.util.extensions.dpToPx
import common.util.extensions.setBackgroundTint
import common.util.extensions.within
import injection.appComponent
import kotlinx.android.synthetic.main.theme_picker_activity.*
import kotlinx.android.synthetic.main.theme_picker_hsl.*
import javax.inject.Inject


class ThemePickerActivity : QkThemedActivity<ThemePickerViewModel>(), ThemePickerView {

    override val viewModelClass = ThemePickerViewModel::class
    override val themeSelectedIntent by lazy { themeAdapter.colorSelected }

    @Inject lateinit var themeAdapter: ThemeAdapter
    @Inject lateinit var themePagerAdapter: ThemePagerAdapter

    private val swatchPadding by lazy { 18.dpToPx(this) }

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.theme_picker_activity)
        setTitle(R.string.title_theme)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        pager.offscreenPageLimit = 1
        pager.adapter = themePagerAdapter
        tabs.pager = pager

        themeAdapter.data = colors.materialColors

        materialColors.layoutManager = LinearLayoutManager(this)
        materialColors.adapter = themeAdapter

        colors.background
                .autoDisposable(scope())
                .subscribe { color -> window.decorView.setBackgroundColor(color) }

        var dX = 0f
        var dY = 0f

        saturation.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = event.x - event.rawX
                    dY = event.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    val min = saturation.x - swatch.width / 2
                    val max = min + saturation.width

                    swatch.x = (event.rawX + dX + swatchPadding).within(min, max)
                    swatch.y = (event.rawY + dY + swatchPadding).within(min, max)
                }

                else -> return@setOnTouchListener false
            }
            true
        }
    }

    override fun render(state: ThemePickerState) {
        themeAdapter.threadId = state.threadId
        themeAdapter.selectedColor = state.selectedColor

        saturation.setBackgroundTint(state.hue)
        swatchPreview.setBackgroundTint(state.selectedColor)
    }
}