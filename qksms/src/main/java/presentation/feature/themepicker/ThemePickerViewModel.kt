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

import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.Preferences
import io.reactivex.rxkotlin.plusAssign
import presentation.common.base.QkViewModel
import javax.inject.Inject

class ThemePickerViewModel : QkViewModel<ThemePickerView, ThemePickerState>(ThemePickerState()) {

    @Inject lateinit var prefs: Preferences

    init {
        appComponent.inject(this)

        disposables += prefs.theme.asObservable()
                .subscribe { color -> newState { it.copy(selectedColor = color) } }
    }

    override fun bindView(view: ThemePickerView) {
        super.bindView(view)

        view.themeSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { color -> prefs.theme.set(color) }
    }

}