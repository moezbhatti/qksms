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

import android.content.Intent
import com.f2prateek.rx.preferences2.Preference
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.base.QkViewModel
import injection.appComponent
import io.reactivex.rxkotlin.plusAssign
import util.Preferences
import javax.inject.Inject

class ThemePickerViewModel(intent: Intent) : QkViewModel<ThemePickerView, ThemePickerState>(ThemePickerState()) {

    @Inject lateinit var prefs: Preferences

    private val threadId = intent.extras?.getLong("threadId") ?: 0L
    private val theme: Preference<Int>

    init {
        appComponent.inject(this)

        newState { it.copy(threadId = threadId) }

        theme = prefs.theme(threadId)

        disposables += theme.asObservable()
                .subscribe { color -> newState { it.copy(selectedColor = color) } }
    }

    override fun bindView(view: ThemePickerView) {
        super.bindView(view)

        view.themeSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { color -> theme.set(color) }
    }

}