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
package com.moez.QKSMS.presentation.common.base

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.presentation.Navigator
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.toolbar.*
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class QkActivity<VM : QkViewModel<*, *>> : AppCompatActivity() {

    @Inject lateinit var colors: Colors

    protected abstract val viewModelClass: KClass<VM>
    protected val viewModel: VM by lazy {
        ViewModelProviders.of(this, Navigator.ViewModelFactory(intent))[viewModelClass.java]
    }

    protected val menu: Subject<Menu> = BehaviorSubject.create()
    protected var disposables = CompositeDisposable()

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onNewIntent(intent)

        disposables += colors.appThemeResources
                .subscribe { res -> setTheme(res) }

        disposables += colors.statusBarIcons
                .subscribe { systemUiVisibility -> window.decorView.systemUiVisibility = systemUiVisibility }

        disposables += colors.statusBar
                .subscribe { color -> window.statusBarColor = color }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Update the colours of the menu items
        disposables += Observables.combineLatest(menu, colors.theme, { menu, color ->
            (0 until menu.size())
                    .map { position -> menu.getItem(position) }
                    .forEach { menuItem ->
                        menuItem?.icon?.run {
                            setTint(color)
                            menuItem.icon = this
                        }
                    }
        }).subscribe()

        disposables += colors.textTertiary
                .doOnNext { color -> toolbar?.overflowIcon = toolbar?.overflowIcon?.apply { setTint(color) } }
                .doOnNext { color -> toolbar?.navigationIcon = toolbar?.navigationIcon?.apply { setTint(color) } }
                .subscribe()

        disposables += colors.popupThemeResource
                .subscribe { res -> toolbar?.popupTheme = res }

        disposables += colors.toolbarColor
                .subscribe { color -> toolbar?.setBackgroundColor(color) }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setSupportActionBar(toolbar)
        title = title // The title may have been set before layout inflation
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        toolbarTitle?.text = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        if (menu != null) {
            this.menu.onNext(menu)
        }
        return result
    }

}