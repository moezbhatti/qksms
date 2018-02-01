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
package presentation.feature.themepicker

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import io.reactivex.Observable
import kotlinx.android.synthetic.main.theme_picker_activity.*
import presentation.common.base.QkActivity
import javax.inject.Inject

class ThemePickerActivity : QkActivity<ThemePickerViewModel>(), ThemePickerView {

    override val viewModelClass = ThemePickerViewModel::class
    override val themeSelectedIntent: Observable<Int> by lazy { themeAdapter.colorSelected }

    @Inject lateinit var themeAdapter: ThemeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.theme_picker_activity)
        setTitle(R.string.title_theme)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        themeAdapter.data = colors.materialColors

        materialColors.layoutManager = LinearLayoutManager(this)
        materialColors.adapter = themeAdapter

        colors.background
                .autoDisposable(scope())
                .subscribe { color -> window.decorView.setBackgroundColor(color) }
    }

    override fun render(state: ThemePickerState) {
        themeAdapter.selectedColor = state.selectedColor
    }

}