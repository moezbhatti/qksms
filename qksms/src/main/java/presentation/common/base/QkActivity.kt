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
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.util.Colors
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.toolbar.*
import presentation.common.Navigator
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class QkActivity<VM : QkViewModel<*, *>> : AppCompatActivity() {

    @Inject lateinit var colors: Colors

    protected abstract val viewModelClass: KClass<VM>
    protected val viewModel: VM by lazy {
        ViewModelProviders.of(this, Navigator.ViewModelFactory(intent))[viewModelClass.java]
    }

    protected val menu: Subject<Menu> = BehaviorSubject.create()

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onNewIntent(intent)

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