package com.moez.QKSMS.ui.base

import android.arch.lifecycle.LifecycleFragment
import android.os.Bundle
import android.view.*
import com.moez.QKSMS.QKApplication
import com.moez.QKSMS.dagger.AppComponent

abstract class QkFragment : LifecycleFragment() {

    abstract fun provideLayoutRes(): Int
    open fun provideMenuRes(): Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(provideMenuRes() != 0)
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(provideLayoutRes(), container, false)
    }

    final override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (provideMenuRes() != 0) inflater.inflate(provideMenuRes(), menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun getContext(): QkActivity {
        return super.getContext() as QkActivity
    }

    fun getAppComponent(): AppComponent? {
        return (context.application as QKApplication).appComponent
    }

}