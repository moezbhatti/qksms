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

import android.content.Intent
import com.f2prateek.rx.preferences2.Preference
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.Preferences
import io.reactivex.rxkotlin.plusAssign
import presentation.common.base.QkViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ThemePickerViewModel(intent: Intent) : QkViewModel<ThemePickerView, ThemePickerState>(ThemePickerState()) {

    @Inject lateinit var prefs: Preferences

    private val threadId = intent.extras?.getLong("threadId") ?: 0L
    private val theme: Preference<Int>

    init {
        appComponent.inject(this)

        theme = prefs.theme(threadId)

        disposables += theme.asObservable()
                .subscribe { color -> newState { it.copy(selectedColor = color) } }
    }

    override fun bindView(view: ThemePickerView) {
        super.bindView(view)

        view.pageScrolledIntent
                .throttleFirst(16, TimeUnit.MILLISECONDS)
                .map { (position, positionOffset) -> if (position == 0) positionOffset * positionOffset else 1f }
                .autoDisposable(view.scope())
                .subscribe { alpha -> newState { it.copy(rgbAlpha = alpha) } }

        view.themeSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { color -> theme.set(color) }
    }

}