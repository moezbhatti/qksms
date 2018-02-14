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
package presentation.common.base

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.util.Colors
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.toolbar.*
import javax.inject.Inject

/**
 * Base activity that automatically applies any necessary theme theme settings and colors
 *
 * In most cases, this should be used instead of the base QkActivity, except for when
 * an activity does not depend on the theme
 */
abstract class QkThemedActivity<VM : QkViewModel<*, *>> : QkActivity<VM>() {

    @Inject lateinit var colors: Colors

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        colors.appThemeResources
                .autoDisposable(scope())
                .subscribe { res -> setTheme(res) }

        colors.statusBarIcons
                .autoDisposable(scope())
                .subscribe { systemUiVisibility -> window.decorView.systemUiVisibility = systemUiVisibility }

        colors.statusBar
                .autoDisposable(scope())
                .subscribe { color -> window.statusBarColor = color }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Update the colours of the menu items
        Observables.combineLatest(menu, colors.theme, { menu, color ->
            (0 until menu.size())
                    .map { position -> menu.getItem(position) }
                    .forEach { menuItem ->
                        menuItem?.icon?.run {
                            setTint(color)
                            menuItem.icon = this
                        }
                    }
        }).autoDisposable(scope(Lifecycle.Event.ON_DESTROY)).subscribe()

        colors.textTertiary
                .doOnNext { color -> toolbar?.overflowIcon = toolbar?.overflowIcon?.apply { setTint(color) } }
                .doOnNext { color -> toolbar?.navigationIcon = toolbar?.navigationIcon?.apply { setTint(color) } }
                .autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
                .subscribe()

        colors.popupThemeResource
                .autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
                .subscribe { res -> toolbar?.popupTheme = res }

        colors.toolbarColor
                .autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
                .subscribe { color -> toolbar?.setBackgroundColor(color) }
    }

}