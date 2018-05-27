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

import com.f2prateek.rx.preferences2.Preference
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.Navigator
import common.base.QkViewModel
import common.util.BillingManager
import common.util.Colors
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.withLatestFrom
import util.Preferences
import javax.inject.Inject
import javax.inject.Named

class ThemePickerViewModel @Inject constructor(
        @Named("threadId") threadId: Long,
        private val billingManager: BillingManager,
        private val colors: Colors,
        private val navigator: Navigator,
        private val prefs: Preferences
) : QkViewModel<ThemePickerView, ThemePickerState>(ThemePickerState(threadId = threadId)) {

    private val theme: Preference<Int> = prefs.theme(threadId)

    override fun bindView(view: ThemePickerView) {
        super.bindView(view)

        theme.asObservable()
                .autoDisposable(view.scope())
                .subscribe { color -> view.setCurrentTheme(color) }

        // Update the theme when a material theme is clicked
        view.themeSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { color -> theme.set(color) }

        // Update the color of the apply button
        view.hsvThemeSelectedIntent
                .doOnNext { color -> newState { it.copy(newColor = color) } }
                .map { color -> colors.textPrimaryOnThemeForColor(color) }
                .doOnNext { color -> newState { it.copy(newTextColor = color) } }
                .autoDisposable(view.scope())
                .subscribe()

        // Toggle the visibility of the apply group
        Observables.combineLatest(theme.asObservable(), view.hsvThemeSelectedIntent, { old, new -> old != new })
                .autoDisposable(view.scope())
                .subscribe { themeChanged -> newState { it.copy(applyThemeVisible = themeChanged) } }

        // Update the theme, when apply is clicked
        view.hsvThemeAppliedIntent
                .withLatestFrom(view.hsvThemeSelectedIntent, { _, color -> color })
                .withLatestFrom(billingManager.upgradeStatus, { color, upgraded ->
                    if (!upgraded) {
                        view.showQksmsPlusSnackbar()
                    } else {
                        theme.set(color)
                    }
                })
                .autoDisposable(view.scope())
                .subscribe()

        // Show QKSMS+ activity
        view.viewQksmsPlusIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showQksmsPlusActivity() }

        // Reset the theme
        view.hsvThemeClearedIntent
                .withLatestFrom(theme.asObservable(), { _, color -> color })
                .autoDisposable(view.scope())
                .subscribe { color -> view.setCurrentTheme(color) }
    }

}