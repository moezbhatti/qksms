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
package common.base

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.util.Colors
import common.util.extensions.setBackgroundTint
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
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

    /**
     * In case the activity should be themed for a specific conversation, the selected conversation
     * can be changed by pushing the threadId to this subject
     */
    protected val threadId: Subject<Long> = BehaviorSubject.createDefault(0)

    /**
     * Switch the theme if the threadId changes
     */
    protected val theme = threadId
            .distinctUntilChanged()
            .switchMap { threadId -> colors.themeForConversation(threadId) }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getAppThemeResourcesObservable()
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
        Observables.combineLatest(menu, theme, colors.textTertiary, { menu, theme, textTertiary ->
            (0 until menu.size())
                    .map { position -> menu.getItem(position) }
                    .forEach { menuItem ->
                        menuItem?.icon?.run {
                            setTint(when (menuItem.itemId) {
                                R.id.info -> textTertiary
                                else -> theme
                            })

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
                .subscribe { color -> toolbar?.setBackgroundTint(color) }
    }

    /**
     * This can be overridden in case an activity does not want to use the default themes
     */
    open fun getAppThemeResourcesObservable() = colors.appThemeResources

}