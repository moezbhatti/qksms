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
package com.moez.QKSMS.common.base

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.moez.QKSMS.R
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
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
abstract class QkThemedActivity : QkActivity() {

    @Inject lateinit var colors: Colors
    @Inject lateinit var prefs: Preferences

    /**
     * In case the activity should be themed for a specific conversation, the selected conversation
     * can be changed by pushing the threadId to this subject
     */
    val threadId: Subject<Long> = BehaviorSubject.createDefault(0)

    /**
     * Switch the theme if the threadId changes
     */
    val theme = threadId
            .distinctUntilChanged()
            .switchMap { threadId -> colors.themeObservable(threadId) }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {

        val night = prefs.night.get()
        val black = prefs.black.get()
        setTheme(getActivityThemeRes(night, black))

        super.onCreate(savedInstanceState)

        // When certain preferences change, we need to recreate the activity
        Observable.merge(
                listOf(prefs.night, prefs.black, prefs.textSize, prefs.systemFont).map { it.asObservable().skip(1) })
                .autoDisposable(scope())
                .subscribe { recreate() }

        // Set the color for the status bar icons
        // If night mode, or no dark icons supported, use light icons
        // If night mode and only dark status icons supported, use dark status icons
        // If night mode and all dark icons supported, use all dark icons
        window.decorView.systemUiVisibility = when {
            night || Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> 0
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else -> View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        // Some devices don't let you modify android.R.attr.navigationBarColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = resolveThemeColor(android.R.attr.windowBackground)
        }

        // Set the color for the overflow and navigation icon
        val textTertiary = resolveThemeColor(android.R.attr.textColorTertiary)
        toolbar?.overflowIcon = toolbar?.overflowIcon?.apply { setTint(textTertiary) }
        toolbar?.navigationIcon = toolbar?.navigationIcon?.apply { setTint(textTertiary) }

        // Set the color for the recent apps title
        val toolbarColor = resolveThemeColor(R.attr.colorPrimary)
        val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val taskDesc = ActivityManager.TaskDescription(getString(R.string.app_name), icon, toolbarColor)
        setTaskDescription(taskDesc)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Update the colours of the menu items
        Observables.combineLatest(menu, theme) { menu, theme ->
            val text = resolveThemeColor(android.R.attr.textColorSecondary)
            (0 until menu.size())
                    .map { position -> menu.getItem(position) }
                    .forEach { menuItem ->
                        menuItem?.icon?.run {
                            setTint(when (menuItem.itemId) {
                                in getColoredMenuItems() -> theme.theme
                                else -> text
                            })

                            menuItem.icon = this
                        }
                    }
        }.autoDisposable(scope(Lifecycle.Event.ON_DESTROY)).subscribe()
    }

    open fun getColoredMenuItems(): List<Int> {
        return listOf()
    }

    /**
     * This can be overridden in case an activity does not want to use the default themes
     */
    open fun getActivityThemeRes(night: Boolean, black: Boolean) = when {
        night && black -> R.style.AppThemeBlack
        night && !black -> R.style.AppThemeDark
        else -> R.style.AppThemeLight
    }

}