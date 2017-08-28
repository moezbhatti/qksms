package com.moez.QKSMS.ui.base

import android.arch.lifecycle.LifecycleFragment
import com.moez.QKSMS.QKApplication
import com.moez.QKSMS.dagger.AppComponent

abstract class QkFragment : LifecycleFragment() {

    override fun getContext(): QkActivity {
        return super.getContext() as QkActivity
    }

    fun getAppComponent(): AppComponent? {
        return (context.application as QKApplication).appComponent
    }

}