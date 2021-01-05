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

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.viewbinding.ViewBinding
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.widget.QkTextView
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

abstract class QkController<ViewContract : QkViewContract<State>, State, Presenter : QkPresenter<ViewContract, State>, Binding : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> Binding
) : LifecycleController() {

    abstract var presenter: Presenter

    private val appCompatActivity: AppCompatActivity?
        get() = activity as? AppCompatActivity

    protected val themedActivity: QkThemedActivity?
        get() = activity as? QkThemedActivity

    protected val toolbar: Toolbar?
        get() = appCompatActivity?.supportActionBar as? Toolbar

    protected val menu: Subject<Menu> = BehaviorSubject.create()
    private val toolbarTitle by lazy { view?.findViewById<QkTextView>(R.id.toolbarTitle) }

    lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = bindingInflater(inflater, container, false)
        binding.root.findViewById<Toolbar?>(R.id.toolbar)?.let { appCompatActivity?.setSupportActionBar(it) }
        onViewCreated()
        return binding.root
    }

    open fun onViewCreated() {
    }

    fun setTitle(@StringRes titleId: Int) {
        setTitle(activity?.getString(titleId))
    }

    fun setTitle(title: CharSequence?) {
        activity?.title = title
        toolbarTitle?.text = title
    }

    fun showBackButton(show: Boolean) {
        appCompatActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu.onNext(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val textSecondary = activity!!.resolveThemeColor(android.R.attr.textColorSecondary)
        toolbar?.overflowIcon = toolbar?.overflowIcon?.apply { setTint(textSecondary) }

        menu.forEach { menuItem -> menuItem.icon.setTint(textSecondary) }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onCleared()
    }

}
