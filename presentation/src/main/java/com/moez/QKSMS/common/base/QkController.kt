package com.moez.QKSMS.common.base

import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.autodispose.ControllerEvent
import com.bluelinelabs.conductor.autodispose.ControllerScopeProvider
import com.uber.autodispose.LifecycleScopeProvider
import kotlinx.android.synthetic.main.toolbar.view.*

abstract class QkController : Controller() {

    private val appCompatActivity: AppCompatActivity?
        get() = activity as? AppCompatActivity

    @CallSuper
    override fun onAttach(view: View) {
        super.onAttach(view)
        appCompatActivity?.setSupportActionBar(view.toolbar)
        setTitle(activity?.title)
    }

    fun setTitle(@StringRes titleId: Int) {
        setTitle(activity?.getString(titleId))
    }

    fun setTitle(title: CharSequence?) {
        activity?.title = title
        view?.toolbarTitle?.text = title
    }

    fun showBackButton(show: Boolean) {
        appCompatActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }

    fun scope(): LifecycleScopeProvider<ControllerEvent> {
        return ControllerScopeProvider.from(this)
    }

}