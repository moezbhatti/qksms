package com.moez.QKSMS.common.base

import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.autodispose.ControllerEvent
import com.bluelinelabs.conductor.autodispose.ControllerScopeProvider
import com.uber.autodispose.LifecycleScopeProvider
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.toolbar.view.*

abstract class QkController<ViewContract : QkViewContract<State>, State, Presenter : QkPresenter<ViewContract, State>> : Controller(), LayoutContainer {

    abstract var presenter: Presenter

    private val appCompatActivity: AppCompatActivity?
        get() = activity as? AppCompatActivity

    override val containerView: View?
        get() = view

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

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        clearFindViewByIdCache()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onCleared()
    }

    fun scope(): LifecycleScopeProvider<ControllerEvent> {
        return ControllerScopeProvider.from(this)
    }

}